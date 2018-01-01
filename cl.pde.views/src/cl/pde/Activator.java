package cl.pde;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin
{
  // The plug-in ID
  public static final String PLUGIN_ID = "cl.pde.views"; //$NON-NLS-1$

  // The shared instance
  private static Activator plugin;

  /**
   * The constructor
   */
  public Activator()
  {
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start(BundleContext context) throws Exception
  {
    super.start(context);
    plugin = this;
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop(BundleContext context) throws Exception
  {
    plugin = null;
    super.stop(context);
  }

  /**
   * Returns the shared instance
   *
   * @return the shared instance
   */
  public static Activator getDefault()
  {
    return plugin;
  }

  /**
   * Initialize ImageRegistry
   */
  @Override
  protected void initializeImageRegistry(ImageRegistry imageRegistry)
  {
    //
    for(Images img : Images.values())
      imageRegistry.put(img.getPath(), getImageDescriptor(img.getPath()));
  }

  /**
   * Returns an image descriptor for the image path at the given plug-in relative path.
   * @param imagePath the image path
   * @return the image descriptor
   */
  public static ImageDescriptor getImageDescriptor(String imagePath)
  {
    return imageDescriptorFromPlugin(PLUGIN_ID, imagePath);
  }

  /**
   * Returns an image descriptor for the image
   * @param img the image path
   * @return the image descriptor
   */
  public static ImageDescriptor getImageDescriptor(Images img)
  {
    return plugin.getImageRegistry().getDescriptor(img.getPath());
  }

  /**
   * Returns an image for the image key
   * @param imageKey The image key
   * @return the image
   */
  public static Image getImage(Images img)
  {
    return plugin.getImageRegistry().get(img.getPath());
  }

  /**
   * Convenience method for logging exceptions in the workbench
   * @param severity the severity
   * @param message the message to display
   * @param e the exception thrown
   */
  public static void log(int severity, String message, Throwable e)
  {
    ILog log = plugin != null? plugin.getLog() : Platform.getLog(FrameworkUtil.getBundle(Activator.class));
    log = log != null? log : IDEWorkbenchPlugin.getDefault().getLog();

    log.log(new Status(severity, PLUGIN_ID, message, e));
  }

  /**
   * Convenience method for logging events in the workbench
   * @param severity the severity
   * @param message the message to display
   */
  public static void log(int severity, String message)
  {
    log(severity, message, null);
  }

  /**
   * Convenience method for logging errors caused by exceptions in the workbench
   * @param message the message to display
   * @param e the exception thrown
   */
  public static void logError(String message, Throwable e)
  {
    log(IStatus.ERROR, message, e);
  }

  /**
   * Convenience method for logging errors caused by exceptions in the workbench
   * @param message the message to display
   */
  public static void logError(String message)
  {
    log(IStatus.ERROR, message);
  }

  /**
   * Convenience method for logging warnings caused by exceptions in the workbench
   * @param message the message to display
   */
  public static void logWarning(String message)
  {
    log(IStatus.WARNING, message);
  }

  /**
   * Convenience method for logging information in the workbench
   * @param message the message to display
   */
  public static void logInfo(String message)
  {
    log(IStatus.INFO, message);
  }
}
