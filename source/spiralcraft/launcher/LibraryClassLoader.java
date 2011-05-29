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
package spiralcraft.launcher;


import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;

import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedList;

import spiralcraft.main.LauncherClassLoader;

/**
 * <p>Loads classes contained in the Library
 * </p>
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
  public String getClassPath()
  {
    StringBuffer buf=new StringBuffer();
    buf.append(super.getClassPath());
    if (buf.length()>0)
    { buf.append(":");
    }
    buf.append(libraryClasspath.getClassPath());
    return buf.toString();
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
  public Enumeration<URL> getResources(String path)
    throws IOException
  { 
    LinkedList<URL> list=new LinkedList<URL>();
    enumerationToList(list,super.getResources(path));
    enumerationToList(list,libraryClasspath.getResources(path));
    return listToEnumeration(list);
  }
  
  @Override
  public InputStream getResourceAsStream(String path)
  { 
    InputStream in=super.getResourceAsStream(path);
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
