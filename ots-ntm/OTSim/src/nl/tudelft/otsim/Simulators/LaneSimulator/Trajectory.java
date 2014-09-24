package nl.tudelft.otsim.Simulators.LaneSimulator;

/**
 * Trajectory of a vehicle. This is basically an array of <tt>jFCD</tt> objects.
 * Methods provide access to each individual field of the underlying data as an
 * array.
 */
public class Trajectory {

    /** Array of <tt>jFCD</tt> objects. */
    protected java.util.ArrayList<FCD> FCD = new java.util.ArrayList<FCD>();
    
    /** Vehicle of this trajectory. */
    public Vehicle vehicle;
    
    /** Time when last snap-shot of vehicle was stored. */
    protected double tData;
    
    /** Whether the trajectory was initialized. */
    protected boolean tDataSet;
    
    /** Class of the FCD data to use. */
    protected Class<?> FCDclass;

    /**
     * Constructor linking this trajectory to a vehicle.
     * @param veh Vehicle of trajectory.
     * @param FCDclass String of class name, as used in <tt>java.lang.Class.forName(String)</tt>.
     * @throws ClassNotFoundException If given class can not be found.
     */
    public Trajectory(Vehicle veh, String FCDclass) throws ClassNotFoundException {
        vehicle = veh;
        if (FCDclass!=null)
            this.FCDclass = java.lang.Class.forName(FCDclass);
    }

    /**
     * Appends current data of vehicle to an internal array at appropriate interval.
     */
    public void append() {
        if (!tDataSet) {
            tData = vehicle.model.t-vehicle.model.settings.getDouble("trajectoryPeriod");
            tDataSet = true;
        }
        if (vehicle.model.settings.getBoolean("storeTrajectoryData") && 
                vehicle.model.t-tData >= vehicle.model.settings.getDouble("trajectoryPeriod")) {
            // create new jFCD and add to vector
            try {
                FCD.add((FCD) FCDclass.getConstructor(vehicle.getClass()).newInstance(vehicle));
            } catch (Exception e) {
                throw new java.lang.RuntimeException("Could not create an instance of "+FCDclass+
                        ". Make sure it has a constructor with a single jVehicle as input.", e);
            }
            // update last sampling time
            tData = tData + vehicle.model.settings.getDouble("trajectoryPeriod");
        }
    }
    
    /**
     * Transforms the trajectory into a serializable object of class 
     * <tt>jTrajectoryData</tt>. Subclasses should override this method and 
     * return an appropriate subclass of <tt>jTrajectoryData</tt>.
     * @return The trajectory as serializable <tt>jTrajectoryData</tt> object.
     */
    public TrajectoryData asSerializable() {
        return new TrajectoryData(this);
    }
     
    /**
     * Returns the class of the FCD objects.
     * @return Class of the FCD objects.
     */
    public Class<?> getFCDclass() {
        return FCDclass;
    }
    
    /**
     * Returns the given field as an array in its original form.
     * @param field Name of field.
     * @return Given field as an array in its original form.
     */
    public Object get(String field) {
        return get(getField(field));
    }
    
    /**
     * Returns the given field as an array in primitive form.
     * @param field Name of field.
     * @return Given field as an array in primitive form.
     */
    public Object getAsPrimitive(String field) {
        return getAsPrimitive(getField(field));
    }
    
    /**
     * Returns the given field as an array in its original form.
     * @param field Field.
     * @return Given field as an array in its original form.
     */
    public Object get(java.lang.reflect.Field field) {
        return get(field, false);
    }
    
    /**
     * Returns the given field as an array in primitive form.
     * @param field Field.
     * @return Given field as an array in primitive form.
     */
    public Object getAsPrimitive(java.lang.reflect.Field field) {
        return get(field, true);
    }
    
    /**
     * Get field of FCD class with give name.
     * @param field Name of field.
     * @return Field, <tt>null</tt> if not found.
     */
    protected java.lang.reflect.Field getField(String field) {
        for (java.lang.reflect.Field f : FCDclass.getFields())
            if (f.getName().equals(field))
                return f;
        return null;
    }
    
    /**
     * Method to return a field of the underlying FCD objects as an array.
     * @param field Field name.
     * @param asPrimitive Whether the data should be a primitive.
     * @return Array representation of single field in array of FCD objects, 
     * <tt>null</tt> if no underlying data.
     */
    protected Object get(java.lang.reflect.Field field, boolean asPrimitive) {
        int n = FCD.size();
        if (n==0)
            return null;
        try {
            if (!asPrimitive || field.getType().isPrimitive()) {
                if (field.get(FCD.get(0)) instanceof Double) {
                    double[] dat = new double[n];
                    for (int i=0; i<n; i++)
                        dat[i] = field.getDouble(FCD.get(i));
                    return dat;
                } else if (field.get(FCD.get(0)) instanceof Integer) {
                    int[] dat = new int[n];
                    for (int i=0; i<n; i++)
                        dat[i] = field.getInt(FCD.get(i));
                    return dat;
                } else if (field.get(FCD.get(0)) instanceof Boolean) {
                    boolean[] dat = new boolean[n];
                    for (int i=0; i<n; i++)
                        dat[i] = field.getBoolean(FCD.get(i));
                    return dat;
                } else if (field.get(FCD.get(0)) instanceof Character) {
                    char[] dat = new char[n];
                    for (int i=0; i<n; i++)
                        dat[i] = field.getChar(FCD.get(i));
                     return dat;
                } else if (field.get(FCD.get(0)) instanceof Long) {
                    long[] dat = new long[n];
                    for (int i=0; i<n; i++)
                        dat[i] = field.getLong(FCD.get(i));
                    return dat;
                } else if (field.get(FCD.get(0)) instanceof Float) {
                    float[] dat = new float[n];
                    for (int i=0; i<n; i++)
                        dat[i] = field.getFloat(FCD.get(i));
                    return dat;
                } else if (field.get(FCD.get(0)) instanceof Byte) {
                    byte[] dat = new byte[n];
                    for (int i=0; i<n; i++)
                        dat[i] = field.getByte(FCD.get(i));
                     return dat;
                } else if (field.get(FCD.get(0)) instanceof Short) {
                    short[] dat = new short[n];
                    for (int i=0; i<n; i++)
                        dat[i] = field.getShort(FCD.get(i));
                    return dat;
                } else {
                    // Non primitive, original form requested
                    java.util.ArrayList<Object> dat = new java.util.ArrayList<Object>();
                    for (int i=0; i<n; i++)
                        dat.add(field.get(FCD.get(i)));
                    Object b = java.lang.reflect.Array.newInstance(dat.get(0).getClass(), 0);
                    return dat.toArray((Object[]) b);
                }
            }
            // get method 'asPrimitive'
            java.lang.reflect.Method method = FCDclass.getMethod("asPrimitive", 
                    String.class, Object.class);
            // get first output
            Object out = method.invoke(FCD.get(0), field.getName(), field.get(FCD.get(0)));

            if (out instanceof Double) {
                double[] dat = new double[n];
                for (int i=0; i<n; i++)
                    dat[i] = (Double) method.invoke(FCD.get(i), field.getName(), field.get(FCD.get(i)));
                return dat;
            } else if (out instanceof Integer) {
                int[] dat = new int[n];
                for (int i=0; i<n; i++)
                    dat[i] = (Integer) method.invoke(FCD.get(i), field.getName(), field.get(FCD.get(i)));
                return dat;
            } else if (out instanceof Boolean) {
                boolean[] dat = new boolean[n];
                for (int i=0; i<n; i++)
                    dat[i] = (Boolean) method.invoke(FCD.get(i), field.getName(), field.get(FCD.get(i)));
                return dat;
            } else if (out instanceof Character) {
                char[] dat = new char[n];
                for (int i=0; i<n; i++)
                    dat[i] = (Character) method.invoke(FCD.get(i), field.getName(), field.get(FCD.get(i)));
                return dat;
            } else if (out instanceof Long) {
                long[] dat = new long[n];
                for (int i=0; i<n; i++)
                    dat[i] = (Long) method.invoke(FCD.get(i), field.getName(), field.get(FCD.get(i)));
                return dat;
            } else if (out instanceof Float) {
                float[] dat = new float[n];
                for (int i=0; i<n; i++)
                    dat[i] = (Float) method.invoke(FCD.get(i), field.getName(), field.get(FCD.get(i)));
                return dat;
            } else if (out instanceof Byte) {
                byte[] dat = new byte[n];
                for (int i=0; i<n; i++)
                    dat[i] = (Byte) method.invoke(FCD.get(i), field.getName(), field.get(FCD.get(i)));
                return dat;
            } else if (out instanceof Short) {
                short[] dat = new short[n];
                for (int i=0; i<n; i++)
                    dat[i] = (Short) method.invoke(FCD.get(i), field.getName(), field.get(FCD.get(i)));
                return dat;
            }            
        } catch (java.lang.IllegalAccessException iae) {
            throw new java.lang.RuntimeException("Field "+field+" or method asPrimitive() has limited access.", iae);
        } catch (java.lang.reflect.InvocationTargetException ite) {
            throw new java.lang.RuntimeException("Error in method asPrimitive().", ite);
        } catch (java.lang.NoSuchMethodException nsme) {
            throw new java.lang.RuntimeException("Method asPrimitive() no defined.", nsme);
        }
        // Return null in case of error.
        return null;
    }
    
    /**
     * Adds scalar fields to trajectory data by invoking underlying FCD 
     * <tt>addScalars</tt> method.
     * @param t {@link TrajectoryData}; the TrajectoryData of which the addScalars method will be invoked
     */
    public void addScalars(TrajectoryData t) {
        try {
            java.lang.reflect.Method method = FCDclass.getMethod("addScalars",
                    TrajectoryData.class, Vehicle.class);
            method.invoke(FCD.get(0), t, vehicle);
        } catch (java.lang.IllegalAccessException iae) {
            throw new java.lang.RuntimeException("Method addScalars has limited access.", iae);
        } catch (java.lang.reflect.InvocationTargetException ite) {
            throw new java.lang.RuntimeException("Error in method addScalars().", ite);
        } catch (java.lang.NoSuchMethodException nsme) {
            throw new java.lang.RuntimeException("Method addScalars() not defined.", nsme);
        }
    }
}