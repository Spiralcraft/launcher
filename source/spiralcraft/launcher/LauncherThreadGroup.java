//
// Copyright (c) 2009 Michael Toth
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
package spiralcraft.launcher;

import java.util.LinkedList;

public class LauncherThreadGroup
  extends ThreadGroup
{
  private static volatile int NEXT_ID=0;
  private volatile boolean finished=false;
  private LinkedList<Thread> launchThreads=new LinkedList<>();
  
  public LauncherThreadGroup()
  { super("spiralcraft-launcher");
  }
  
  void finish()
  { finished=true;
  }
  
  public void run(Runnable runnable)
  {
    Thread thread=new Thread(this,runnable,"launch-"+(NEXT_ID++));
    launchThreads.add(thread);
    thread.setDaemon(true);
    thread.start();
    try
    {
      while (!finished && thread.isAlive())
      { thread.join(100);
      }
    }
    catch (InterruptedException x)
    {
    }
    thread.setContextClassLoader(null);
    thread=null;
  }
  
  public void join()
  {
    while (true) 
    {
      Thread[] threads=new Thread[activeCount()];
      enumerate(threads);
      for (Thread thread:threads)
      { 
        try
        { thread.join();
        }
        catch (InterruptedException x)
        { x.printStackTrace();
        }
        thread.setContextClassLoader(null);
      }
      if (threads.length==0)
      { break;
      }
      
      threads=null;
    }
  }
}
