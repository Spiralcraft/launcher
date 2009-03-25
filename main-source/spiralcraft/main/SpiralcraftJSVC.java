//
// Copyright (c) 2009,2009 Michael Toth
// Spiralcraft Inc., All Rights Reserved
//
// This package is part of the Spiralcraft project and is licensed under
// a multiple-license framework.
//
// You may not use this file except in compliance with the terms found in the
// SPIRALCRAFT-LICENSE.txt file at the top of this distribution, or available
// at http://www.spiralcraft.org/licensing/SPIRALCRAFT-LICENSE.txt.
//
// Unless otherwise agreed to in writing, this software is distributed on an
// "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
//
package spiralcraft.main;

/**
 * <p>Implements jsvc interface methods for starting Spiralcraft in a
 *   native wrapper
 * </p>
 * 
 * @author mike
 *
 */
public class SpiralcraftJSVC
  extends Spiralcraft
  implements Runnable
{
  
  private Thread mainThread;
  private String[] svcArguments;
  // private int exitCode;
  
  /**
   * Here open the configuration files, create the trace file, create the 
   *   ServerSockets, the Threads
   *  
   * @param arguments
   */
  public void load(String[] arguments)
  {
    mainThread=new Thread(this);
    this.svcArguments=arguments;
    mainThread.start();
  }
  
  /**
   * Start the Thread, accept incoming connections
   */
  public void start()
  {
  }
  
  public void run()
  {
    run(svcArguments);
  }
  
  /**
   * Inform the Thread to live the run(), close the ServerSockets
   */
  public void stop()
  { mainThread.interrupt();
  }
  
  /**
   * Destroy any object created in init()
   */
  public void destroy()
  { 
    mainThread=null;
  }

}
