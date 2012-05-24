//
// Copyright (c) 1998,2009 Michael Toth
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
// 
package spiralcraft.launcher;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;

public abstract class Module
{
  protected String path;
  protected URI uri;
  protected String name;
  protected long lastModified;
  protected HashMap<String,Resource> resources
    =new HashMap<String,Resource>();

  public Module(File file)
    throws IOException
  { 
    path=file.getAbsolutePath();
    uri=file.getAbsoluteFile().toURI();
    name=file.getName();
    lastModified=file.lastModified();
    catalogResources();
  }

  /**
   * Indicate whether the library is a release of the
   *   specified module
   */
  public boolean isModule(String moduleName)
  { 
    // XXX For now, use exact name = HACK
    return name.equals(moduleName) && !(this instanceof NativeLibrary);
  }

  public abstract String[] getLibraryDependencies();

  public abstract void open()
    throws IOException;

  public abstract void close()
    throws IOException;

  public abstract void forceClose()
    throws IOException;

  public abstract void catalogResources()
    throws IOException;

  public String getPath()
  { return path;
  }
  
  public String getName()
  { return name;
  }
  
  public long getLastModified()
  { return lastModified;
  }
  
  public Resource getResource(String name)
  { return resources.get(name);
  }
}

