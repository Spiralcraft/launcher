//
// Copyright (c) 1998,2008 Michael Toth
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
package spiralcraft.loader;


import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;

import java.net.URL;

import spiralcraft.main.LauncherClassLoader;

/**
 * <P>Loads classes contained in the Library
 * </P>
 * 
 */
public class LibraryClassLoader
  extends LauncherClassLoader
{
  private final LibraryClasspath libraryClasspath;

  public LibraryClassLoader(LibraryCatalog libraryCatalog)
  { libraryClasspath=libraryCatalog.createLibraryClasspath();
  }

  @Override
  public void setDebug(boolean debug)
  { 
    super.setDebug(debug);
    libraryClasspath.setDebug(debug);
  }

  
  @Override
  protected String findLibrary(String name)
  { return libraryClasspath.findNativeLibrary(name);
  }
  

  @Override
  protected byte[] loadData(String path)
    throws IOException
  { return libraryClasspath.loadData(path);
  }

  @Override
  protected URL findResource(String path)
  { 
    try
    { return libraryClasspath.getResource(path);
    }
    catch (IOException x)
    { x.printStackTrace();
    }
    return null;
  }
  
  @Override
  public InputStream getResourceAsStream(String path)
  { 
    InputStream in=null;
    ClassLoader parent=getParent();
    if (parent!=null)
    { in=parent.getResourceAsStream(path);
    }
    if (in==null)
    { 
      try
      { in=new ByteArrayInputStream(loadData(path));
      }
      catch (IOException x)
      { }
    }
    return in;
  }

  public void resolveLibrariesForClass(String className)
    throws IOException
  {
    String resourceName=className.replace('.','/')+".class";
    libraryClasspath.resolveLibrariesForResource(resourceName);
  }

  /**
   * Adds the libraries associated with this module and its
   *   dependents to the classpath
   */
  public void addModule(String moduleName)
    throws IOException
  { libraryClasspath.addModule(moduleName);
  }
  
  
  /**
   * Add the latest versions of all the modules in the library
   *   to the class path
   */
  public void addAllModules()
    throws IOException
  { libraryClasspath.addAllModules();
  }
  
  @Override
  public void shutdown()
  { libraryClasspath.release();
  }
}
