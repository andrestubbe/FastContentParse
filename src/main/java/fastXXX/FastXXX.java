package fastXXX;

import fastcore.FastCore;

/**
 * FastXXX Main API Class.
 * Native Windows capabilities exposed via JNI.
 */
public class FastXXX {

    // Load the native library once upon class initialization
    static {
        FastCore.loadLibrary("fastXXX");
    }

    /**
     * Executes the native capability via C++.
     */
    public native void doSomethingNative();

}
