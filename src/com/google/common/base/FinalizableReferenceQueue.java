package com.google.common.base;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FinalizableReferenceQueue
{
  private static final String FINALIZER_CLASS_NAME = "com.google.common.base.internal.Finalizer";
  private static final Logger logger = Logger.getLogger(FinalizableReferenceQueue.class.getName());
  private static final Method startFinalizer = getStartFinalizer(loadFinalizer(arrayOfFinalizerLoader));
  final ReferenceQueue<Object> queue;
  final boolean threadStarted;

  static
  {
    FinalizerLoader[] arrayOfFinalizerLoader = new FinalizerLoader[3];
    arrayOfFinalizerLoader[0] = new SystemLoader();
    arrayOfFinalizerLoader[1] = new DecoupledLoader();
    arrayOfFinalizerLoader[2] = new DirectLoader();
  }

  public FinalizableReferenceQueue()
  {
    try
    {
      localReferenceQueue = (ReferenceQueue)startFinalizer.invoke(null, new Object[] { FinalizableReference.class, this });
      bool = true;
      this.queue = localReferenceQueue;
      this.threadStarted = bool;
      return;
    }
    catch (IllegalAccessException localIllegalAccessException)
    {
      throw new AssertionError(localIllegalAccessException);
    }
    catch (Throwable localThrowable)
    {
      while (true)
      {
        logger.log(Level.INFO, "Failed to start reference finalizer thread. Reference cleanup will only occur when new references are created.", localThrowable);
        ReferenceQueue localReferenceQueue = new ReferenceQueue();
        boolean bool = false;
      }
    }
  }

  static Method getStartFinalizer(Class<?> paramClass)
  {
    try
    {
      Method localMethod = paramClass.getMethod("startFinalizer", new Class[] { Class.class, Object.class });
      return localMethod;
    }
    catch (NoSuchMethodException localNoSuchMethodException)
    {
      throw new AssertionError(localNoSuchMethodException);
    }
  }

  private static Class<?> loadFinalizer(FinalizerLoader[] paramArrayOfFinalizerLoader)
  {
    int i = paramArrayOfFinalizerLoader.length;
    for (int j = 0; j < i; j++)
    {
      Class localClass = paramArrayOfFinalizerLoader[j].loadFinalizer();
      if (localClass != null)
        return localClass;
    }
    throw new AssertionError();
  }

  void cleanUp()
  {
    if (this.threadStarted)
      return;
    while (true)
    {
      Reference localReference = this.queue.poll();
      if (localReference == null)
        break;
      localReference.clear();
      try
      {
        ((FinalizableReference)localReference).finalizeReferent();
      }
      catch (Throwable localThrowable)
      {
        logger.log(Level.SEVERE, "Error cleaning up after reference.", localThrowable);
      }
    }
  }

  static class DecoupledLoader
    implements FinalizableReferenceQueue.FinalizerLoader
  {
    private static final String LOADING_ERROR = "Could not load Finalizer in its own class loader. Loading Finalizer in the current class loader instead. As a result, you will not be able to garbage collect this class loader. To support reclaiming this class loader, either resolve the underlying issue, or move Google Collections to your system class path.";

    URL getBaseUrl()
      throws IOException
    {
      String str1 = "com.google.common.base.internal.Finalizer".replace('.', '/') + ".class";
      URL localURL = getClass().getClassLoader().getResource(str1);
      if (localURL == null)
        throw new FileNotFoundException(str1);
      String str2 = localURL.toString();
      if (!str2.endsWith(str1))
        throw new IOException("Unsupported path style: " + str2);
      return new URL(localURL, str2.substring(0, str2.length() - str1.length()));
    }

    public Class<?> loadFinalizer()
    {
      try
      {
        Class localClass = newLoader(getBaseUrl()).loadClass("com.google.common.base.internal.Finalizer");
        return localClass;
      }
      catch (Exception localException)
      {
        FinalizableReferenceQueue.logger.log(Level.WARNING, "Could not load Finalizer in its own class loader. Loading Finalizer in the current class loader instead. As a result, you will not be able to garbage collect this class loader. To support reclaiming this class loader, either resolve the underlying issue, or move Google Collections to your system class path.", localException);
      }
      return null;
    }

    URLClassLoader newLoader(URL paramURL)
    {
      return new URLClassLoader(new URL[] { paramURL });
    }
  }

  static class DirectLoader
    implements FinalizableReferenceQueue.FinalizerLoader
  {
    public Class<?> loadFinalizer()
    {
      try
      {
        Class localClass = Class.forName("com.google.common.base.internal.Finalizer");
        return localClass;
      }
      catch (ClassNotFoundException localClassNotFoundException)
      {
        throw new AssertionError(localClassNotFoundException);
      }
    }
  }

  static abstract interface FinalizerLoader
  {
    public abstract Class<?> loadFinalizer();
  }

  static class SystemLoader
    implements FinalizableReferenceQueue.FinalizerLoader
  {
    // ERROR //
    public Class<?> loadFinalizer()
    {
      // Byte code:
      //   0: invokestatic 22	java/lang/ClassLoader:getSystemClassLoader	()Ljava/lang/ClassLoader;
      //   3: astore_2
      //   4: aload_2
      //   5: ifnull +28 -> 33
      //   8: aload_2
      //   9: ldc 24
      //   11: invokevirtual 28	java/lang/ClassLoader:loadClass	(Ljava/lang/String;)Ljava/lang/Class;
      //   14: astore 4
      //   16: aload 4
      //   18: areturn
      //   19: astore_1
      //   20: invokestatic 34	com/google/common/base/FinalizableReferenceQueue:access$000	()Ljava/util/logging/Logger;
      //   23: ldc 36
      //   25: invokevirtual 42	java/util/logging/Logger:info	(Ljava/lang/String;)V
      //   28: aconst_null
      //   29: areturn
      //   30: astore_3
      //   31: aconst_null
      //   32: areturn
      //   33: aconst_null
      //   34: areturn
      //
      // Exception table:
      //   from	to	target	type
      //   0	4	19	java/lang/SecurityException
      //   8	16	30	java/lang/ClassNotFoundException
    }
  }
}

/* Location:           C:\Users\科\Desktop\classes_dex2jar.jar
 * Qualified Name:     com.google.common.base.FinalizableReferenceQueue
 * JD-Core Version:    0.6.2
 */