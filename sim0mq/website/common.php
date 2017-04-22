<?php
function highlightItem ($text, $condition, $link)
{
        echo "        <li";
        if ($condition)
        {
                echo " class=\"highlight\"";
        }
        echo "><a href=\"$link\">$text</a></li>\n";
}
function bodyStart ($pageName)
{
        echo "  <!-- ======== Header ======== -->\n";
        echo "\n";
        echo "  <table width=\"100%\" cellpadding=\"0\">\n";
        echo "    <tr style=\"line-height:1px; height:60px\">\n";
        echo "      <td style=\"background-color: #0094FE\">\n";
        echo "        <p style=\"font-color:BLACK; font-size:30px\"> <b>&nbsp;Simulation Message Bus on 0MQ, version 0.01 &nbsp;</b>\n";
        echo "        </p>\n";
        echo "      </td>\n";
        echo "    </tr>\n";
        echo "  </table>\n";
        echo "\n";
        echo "  <!-- ======== Left Sidebar ======== -->\n";
        echo "\n";
        echo "  <div id=\"sidebar\">\n";
        echo "    <div>\n";
        echo "      <p class=\"title\">\n";
        echo "        <a href=\"index.html\">SIM0MQ</a>\n";
        echo "      </p>\n";
        echo "      <ul>\n";
        highlightItem("Overview", "index" == $pageName, "index.php");
        highlightItem("Source code", "source" == $pageName, "source.php");
        highlightItem("Maven use", "maven" == $pageName, "maven.php");
        echo "      </ul>\n";
        echo "    </div>\n";
        echo "  </div>\n";
        echo "\n";
        echo "  <!-- ======== Main Content ======== -->\n";
        echo "\n";
        echo "  <div id=\"main\">\n";
        echo "\n";
}
?>
