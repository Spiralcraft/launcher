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

import spiralcraft.app.spi.AbstractComponent;
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
}
