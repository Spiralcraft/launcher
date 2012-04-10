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

  
  private PrintStream outStream;
  private PrintStream errStream;
  private InputStream inStream;
  
  private SimpleState rootState;

  private volatile boolean stopped=false;
  
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
        @Override
        public PrintStream out()
        { return outStream!=null?outStream:super.out();
        }

        @Override
        public PrintStream err()
        { return errStream!=null?errStream:super.err();
        }

        @Override
        public InputStream in()
        { return inStream!=null?inStream:super.in();
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
      new StandardDispatcher(true,new StateFrame())
        .dispatch(DisposeMessage.INSTANCE,this,rootState,null);
      super.stop();
      stopped=true;
    }
    
  }
}
