package org.opentrafficsim;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

/**
 * Utility to add or update the type foe each parameter in the javadoc of all java files in /src/main/java in all or in selected
 * projects in the workspace. Run this utility only from Eclipse!<br>
 * <br>
 * Copyright (c) 2003-2019 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class ParamComments
{
    /** the lines of the file. */
    private List<String> lines;

    /** file changed? */
    private boolean changed;

    /**
     * @param args String[]; none
     * @throws IOException on I/O error
     * @throws URISyntaxException on I/O error
     */
    public static void main(final String[] args) throws IOException, URISyntaxException
    {
        new ParamComments();
    }

    /**
     * @throws IOException on I/O error
     * @throws URISyntaxException on I/O error
     */
    public ParamComments() throws IOException, URISyntaxException
    {
        File classFolder = new File(ParamComments.class.getResource("/").toURI());
        File workspaceFolder = classFolder.getParentFile().getParentFile().getParentFile();
        for (File projectFolder : workspaceFolder.listFiles())
        {
            if (projectFolder.isDirectory() && projectFolder.getName().startsWith("ots")
                    && new File(projectFolder, "src/main/java").exists())
            {
                File sourcePathFile = new File(projectFolder, "src/main/java");
                for (File srcFolder : sourcePathFile.listFiles())
                {
                    processDirOrFile(srcFolder);
                }
            }
        }
    }

    /**
     * @param srcFolder File; folder to look for subfolders and/or java files
     * @throws IOException on i/o error
     */
    private void processDirOrFile(final File srcFolder) throws IOException
    {
        if (srcFolder.isDirectory())
        {
            for (File subFile : srcFolder.listFiles())
            {
                if (subFile.isDirectory())
                {
                    processDirOrFile(subFile);
                }
                else if (subFile.getName().endsWith(".java") && !subFile.getName().startsWith("package-info")
                        && !srcFolder.getName().contains("generated") && !subFile.getName().contains("ProtoBuf")
                        && !srcFolder.getName().contains(".proto"))
                {
                    processJavaFile(subFile);
                }
            }
        }
    }

    /**
     * @param javaFile File; java file to process
     * @throws IOException on error
     */
    private void processJavaFile(final File javaFile) throws IOException
    {
        System.out.println("\n" + javaFile.toURI().getPath());
        this.changed = false;
        this.lines = Files.readAllLines(Paths.get(javaFile.toURI()), StandardCharsets.UTF_8);
        FileInputStream in = new FileInputStream(javaFile.toURI().getPath());
        CompilationUnit cu = JavaParser.parse(in);
        cu.accept(new CodeVisitor(this), null);
        if (this.changed)
        {
            Files.write(Paths.get(javaFile.toURI()), this.lines);
            System.out.println("CHANGED AND WRITTEN: " + javaFile.toString());
        }
    }

    /**
     * @return changed
     */
    public final boolean isChanged()
    {
        return this.changed;
    }

    /**
     * @param changed boolean; set changed
     */
    public final void setChanged(final boolean changed)
    {
        this.changed = changed;
    }

    /**
     * @return lines
     */
    public final List<String> getLines()
    {
        return this.lines;
    }

    /**
     * Simple visitor implementation for visiting MethodDeclaration and ConstructorDeclaration nodes.
     */
    protected static class CodeVisitor extends VoidVisitorAdapter<Void>
    {
        /** access to the lines of the file and to changed toggle. */
        private ParamComments paramComments;

        /**
         * Constructor of the code visitor.
         * @param paramComments ParamComments; class to be processed
         */
        protected CodeVisitor(final ParamComments paramComments)
        {
            this.paramComments = paramComments;
        }

        @Override
        /**
         * {@inheritDoc} <br>
         * This method will be called for all constructors in this CompilationUnit, including constructors of inner classes.
         */
        public void visit(final ConstructorDeclaration constructorDeclaration, final Void arg)
        {
            System.out.println("\n------\nCONSTRUCTOR\n" + "   " + constructorDeclaration.getName() + " : "
                    + constructorDeclaration.getDeclarationAsString());
            processDeclaration(constructorDeclaration);
            super.visit(constructorDeclaration, arg);
        }

        @Override
        /**
         * {@inheritDoc} <br>
         * This method will be called for all methods in this CompilationUnit, including inner class methods.
         */
        public void visit(final MethodDeclaration methodDeclaration, final Void arg)
        {
            System.out
                    .println("\n------\n" + "   " + methodDeclaration.getName() + " : " + methodDeclaration.getTypeAsString());
            processDeclaration(methodDeclaration);
            super.visit(methodDeclaration, arg);
        }

        /**
         * Carry out the changes in the method comments or constructor comments.
         * @param callableDeclaration CallableDeclaration&lt;?&gt;; the method declaration or constructor declaration
         */
        private void processDeclaration(final CallableDeclaration<?> callableDeclaration)
        {
            for (Parameter parameter : callableDeclaration.getParameters())
            {
                System.out.println("      " + parameter.getNameAsString() + " : " + parameter.getTypeAsString()
                        + (parameter.isVarArgs() ? "..." : ""));
            }
            if (callableDeclaration.getComment().isPresent())
            {
                System.out.print(callableDeclaration.getComment().get());
                String parserComment = callableDeclaration.getComment().get().toString();
                if (parserComment.contains("@param"))
                {
                    // see how we would rebuild the comment
                    if (callableDeclaration.getComment().get().toJavadocComment().isPresent())
                    {
                        JavadocComment comment = callableDeclaration.getComment().get().toJavadocComment().get();
                        String[] commentLines = parserComment.split("\n");
                        for (String line : commentLines)
                        {
                            if (line.contains("@param") && !line.contains("@param <"))
                            {
                                // which line is it in the String[] model?
                                int fileLine = -1;
                                for (int lnr = 0; lnr < commentLines.length; lnr++)
                                {
                                    if (this.paramComments.getLines().get(comment.getBegin().get().line + lnr).trim()
                                            .equals(line.trim()))
                                    {
                                        fileLine = comment.getBegin().get().line + lnr;
                                    }
                                }
                                if (fileLine == -1)
                                {
                                    System.out.println("COULD NOT FIND LINE FROM COMMENT IN FILE LINES...");
                                    break;
                                }
                                line = line.replaceAll("&lt;", "<").replaceAll("&gt;", ">");
                                // make a line without escaped HTML-sequences
                                String noHtmlLine = line.replaceAll("&\\w+;", "");
                                // variable name after @param
                                int paramIndex = line.indexOf("@param");
                                int varEndIndex = line.indexOf(' ', paramIndex + 7) == -1 ? line.length()
                                        : line.indexOf(' ', paramIndex + 7);
                                String varName = line.substring(paramIndex + 6, varEndIndex).trim();
                                boolean found = false;
                                for (Parameter parameter : callableDeclaration.getParameters())
                                {
                                    if (parameter.getNameAsString().equals(varName))
                                    {
                                        // see if type is there with a ; at the end
                                        String varType = "";
                                        if (noHtmlLine.indexOf(";", varEndIndex + 1) != -1)
                                        {
                                            varType = noHtmlLine
                                                    .substring(varEndIndex + 1, noHtmlLine.indexOf(';', varEndIndex + 1)).trim()
                                                    .replaceAll(", ", ",");
                                            String parameterType =
                                                    parameter.getType().asString() + (parameter.isVarArgs() ? "..." : "");
                                            // if there are spaces in the varType, we either have a type with spaces
                                            // e.g., Type<A, B>, or we have no variable but a ; later in the line,
                                            // e.g., @param var this is the B&eacute;zier variable. In these cases,
                                            // we have to be super careful and not replace without warning...
                                            if (!varType.equals(parameterType))
                                            {
                                                if (varType.contains(" "))
                                                {
                                                    System.out.println("NO CHANGE - SPACES IN TYPE : "
                                                            + line.replaceAll("<", "&lt;").replaceAll(">", "&gt;").trim());
                                                }
                                                else
                                                {
                                                    parameterType = parameterType.replaceAll(",", ", ");
                                                    line = line.substring(0, varEndIndex) + " " + parameterType
                                                            + line.substring(line.indexOf(";", varEndIndex + 1));
                                                    line = line.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
                                                    System.out.println("CHANGED TYPE : " + line.trim());
                                                    this.paramComments.setChanged(true);
                                                    this.paramComments.getLines().set(fileLine, line.replaceAll("\\n", ""));
                                                }
                                            }
                                        }
                                        else
                                        {
                                            String parameterType = parameter.getType().asString()
                                                    + (parameter.isVarArgs() ? "..." : "").replaceAll(",", ", ");
                                            line = line.substring(0, varEndIndex) + " " + parameterType + "; "
                                                    + line.substring(Math.min(varEndIndex + 1, line.length() - 1));
                                            line = line.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
                                            System.out.println("ADDED TYPE : " + line.trim());
                                            this.paramComments.setChanged(true);
                                            this.paramComments.getLines().set(fileLine, line.replaceAll("\\n", ""));
                                        }
                                        found = true;
                                        break;
                                    }
                                }
                                if (!found)
                                {
                                    System.out.println("XXXXXXXX @param comment for " + varName
                                            + " does not match any parameters in method");
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
