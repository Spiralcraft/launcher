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
package spiralcraft.main;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads classes contained in a specific jar
 */
public class LauncherClassLoader
  extends ClassLoader
{
  
  protected boolean debug;
  private List<ClassResource> resources
    =new ArrayList<ClassResource>();

  public LauncherClassLoader()
  { super(LauncherClassLoader.class.getClassLoader());
  }
  
  public void addResource(ClassResource resource)
  { resources.add(resource);
  }

  public void setDebug(boolean debug)
  { this.debug=debug;
  }
  
  @Override
  public Class<?> findClass(String name)
    throws ClassNotFoundException
  { 
    byte[] classData=loadClassData(name);
    if (classData!=null)
    { return defineClass(name,classData,0,classData.length);
    }
    else
    { throw new ClassNotFoundException(name);
    }
  }

  private byte[] loadClassData(String name)
  {
    try
    {
      String path = name.replace('.', '/')+".class";
      return loadData(path);
    }
    catch (IOException x)
    { return null;
    }
  }

  @Override
  public InputStream getResourceAsStream(String path)
  {
    for (ClassResource resource:resources)
    {
      InputStream data=resource.getResourceAsStream(path);
      if (data!=null)
      { return data;
      }
    }
    return null;
  }
  
  /**
   * Load data from the repository path specified
   */
  protected byte[] loadData(String path)
    throws IOException
  {
    for (ClassResource resource:resources)
    {
      byte[] data=resource.loadData(path);
      if (data!=null)
      { return data;
      }
    }
    return null;
  }
    
  /**
   * Close all resources
   */
  public void shutdown()
  {
    for (ClassResource resource:resources)
    { resource.shutdown();
    }
  }
}
