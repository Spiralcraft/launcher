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
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * <p>Loads classes contained in a specific set of resources
 * </p>
 */
public class LauncherClassLoader
  extends ClassLoader
{
  
  public static final <X> void enumerationToList(List<X> list,Enumeration<X> enumeration)
  {
    if (enumeration!=null)
    {
      while (enumeration.hasMoreElements())
      { list.add(enumeration.nextElement());
      }
    }
  
  }

  public static final <X> Enumeration<X> listToEnumeration(final List<X> list)
  {
    return new Enumeration<X>()
    {
      final Iterator<X> iterator=list.iterator();
      
      @Override
      public boolean hasMoreElements()
      { return iterator.hasNext();
      }
      
      @Override
      public X nextElement()
      { return iterator.next();
      }
    };
  
  }
  
  protected boolean debug;
  private List<ClassResource> resources
    =new ArrayList<ClassResource>();

  /**
   * <p>Construct a LauncherClassLoader that delegates to the default
   *   application ClassLoader which loaded this class.
   * </p>
   */
  public LauncherClassLoader()
  { super(LauncherClassLoader.class.getClassLoader());
  }

  /**
   * <p>Construct a LauncherClassLoader that delegates to a specific parent
   *   ClassLoader
   * </p>
   * 
   * @param parent
   */
  public LauncherClassLoader(ClassLoader parent)
  { super(parent);
  }
  
  public String getClassPath()
  {
    StringBuffer buf=new StringBuffer();
    for (ClassResource resource:resources)
    { 
      if (buf.length()>0)
      { buf.append(":");
      }
      buf.append(resource.getClassPath());
    }
    return buf.toString();
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
  public URL getResource(String path)
  {
    // System.err.println("gr:"+path);
    return super.getResource(path);
  }
  
  @Override
  public InputStream getResourceAsStream(String path)
  {
    // System.err.println("gras:"+path);
    if (getParent()!=null)
    { 
      InputStream in=getParent().getResourceAsStream(path);
      if (in!=null)
      { return in;
      }
    }
    else
    {
      InputStream in=ClassLoader.getSystemClassLoader().getResourceAsStream(path);
      if (in!=null)
      { return in;
      }
    }
    
    for (ClassResource resource:resources)
    {
      InputStream data=resource.getResourceAsStream(path);
      if (data!=null)
      { return data;
      }
    }
    

    return null;
  }
  
  @Override
  protected URL findResource(String path)
  { 
    // System.err.println("fr:"+path);
    for (ClassResource resource:resources)
    {
      URL url=resource.getResource(path);
      if (url!=null)
      { return url;
      }
    }
    return null;
    
  }
  
  @Override
  public Enumeration<URL> getResources(String path)
    throws IOException
  {
    // System.err.println("grs:"+path);
    
    final LinkedList<URL> list=new LinkedList<URL>();
    for (ClassResource resource:resources)
    {
      URL url=resource.getResource(path);
      if (url!=null)
      { list.add(url);
      }
    }
    
    if (getParent()!=null)
    { enumerationToList(list,getParent().getResources(path));
    }
    else 
    { enumerationToList(list,ClassLoader.getSystemClassLoader().getResources(path));
    }

    return listToEnumeration(list);
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


