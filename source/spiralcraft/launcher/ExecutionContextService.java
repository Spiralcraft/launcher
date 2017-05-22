//
// Copyright (c) 2011 Michael Toth
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

import java.io.InputStream;
import java.io.PrintStream;

import spiralcraft.app.DisposeMessage;
import spiralcraft.app.InitializeMessage;
import spiralcraft.app.StateFrame;
import spiralcraft.app.kit.AbstractComponent;
import spiralcraft.app.kit.SimpleState;
import spiralcraft.app.kit.StandardDispatcher;
import spiralcraft.common.LifecycleException;
import spiralcraft.exec.ExecutionContext;
import spiralcraft.io.InputStreamReference;
import spiralcraft.io.PrintStreamReference;
import spiralcraft.service.Service;

/**
 * Sets up an Execution context to handle IO streams for launched
 *   application environments.
 * 
 * @author mike
 *
 */
public class ExecutionContextService
  extends AbstractComponent
  implements ExecutionContextProvider,Service
{

  
  private volatile PrintStream outStream;
  private volatile PrintStream errStream;
  private volatile InputStream inStream;
  
  private SimpleState rootState;

  private volatile boolean stopped=false;
  
  private LauncherThreadGroup launcherGroup;
  
  public void setOutStream(PrintStream outStream)
  { this.outStream=outStream;
  }

  public void setErrStream(PrintStream errStream)
  { this.errStream=errStream;
  }
  
  public void setInStream(InputStream inStream)
  { this.inStream=inStream;
  }

  
  @Override
  public void push()
  {
    ExecutionContext.pushInstance
      (new ExecutionContext(ExecutionContext.getInstance())
      {
        private final ExecutionContext delegate=ExecutionContext.getInstance();
        private final PrintStreamReference out
          =new PrintStreamReference(true)
          {

            @Override
            protected PrintStream get()
            { return outStream!=null?outStream:delegate.out();
            }
          
          };
            
        private final PrintStreamReference err
          =new PrintStreamReference(true)
          {

            @Override
            protected PrintStream get()
            { return errStream!=null?errStream:delegate.err();
            }
          
          };
        
        private final InputStreamReference in
          =new InputStreamReference()
          { 
            protected InputStream get()
            { return inStream!=null?inStream:delegate.in();
            }
          
          };

        @Override
        public PrintStream out()
        { return out;
        }
        
        @Override
        public PrintStream err()
        { return err;
        }

        @Override
        public InputStream in()
        { return in;
        }
        
      }
      );
  }

  @Override
  public void pop()
  { ExecutionContext.popInstance();
  }

  public void setServices(Service[] services)
  { setContents(services);
  }
  
  @Override
  public void start()
    throws LifecycleException
  {
    super.start();
    rootState=new SimpleState(this.asContainer().getChildCount(),this.id);
    new StandardDispatcher(true,new StateFrame())
      .dispatch(InitializeMessage.INSTANCE,this,rootState,null);
    
    Runtime.getRuntime().addShutdownHook
      (new Thread
        (new Runnable() 
          {
            @Override 
            public void run() 
            {shutdownHook();
            } 
          }
        )
      );
    
    stopped=false;
  }
  
  public void dispose()
  {
    launcherGroup.finish();
    new StandardDispatcher(true,new StateFrame())
      .dispatch(DisposeMessage.INSTANCE,this,rootState,null);
  }
  
  public void setLauncherGroup(LauncherThreadGroup group)
  { this.launcherGroup=group;
  }
  
  private void shutdownHook()
  {
    try
    { stop();
    }
    catch (LifecycleException x)
    { throw new RuntimeException(x);
    }
  }
  
  @Override
  public synchronized void stop()
    throws LifecycleException
  { 
    if (!stopped)
    { 
      dispose();
      super.stop();
      stopped=true;
    }
    
  }
}
