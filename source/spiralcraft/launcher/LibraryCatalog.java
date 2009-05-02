//
// Copyright (c) 1998,2005 Michael Toth
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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.BufferedInputStream;

import java.util.Map.Entry;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.util.jar.Manifest;
import java.util.jar.Attributes;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import java.net.URL;

import spiralcraft.log.ClassLog;
import spiralcraft.util.IteratorEnumeration;
import spiralcraft.util.ListMap;
import spiralcraft.util.string.StringUtil;

/**
 * <P>Catalog of the code and resource Libraries available for use by a
 *   ClassLoader. A LibraryCatalog may contain multiple versions of a Library.
 * </P>
 * 
 * <P>TODO: A LibraryCatalog is really an Installation. A Library is a Module.
 * </P>
 */
public class LibraryCatalog
{


  private final String codebaseRootPath;

  private ArrayList<Library> codebaseLibraries=new ArrayList<Library>();
  
  /**
   * Create a new LibraryCatalog for the library located at the specified
   *   File path.
   */
  public LibraryCatalog(File path)
  { 
    codebaseRootPath=path.getAbsolutePath();
    loadCatalog();
  }
  
  public List<Library> listLibraries()
  { return codebaseLibraries;
  }
  
  public void close()
  {
    for (Library library: codebaseLibraries)
    { 
      try
      { library.forceClose();
      }
      catch (IOException x)
      { }
    }
  }
  
  /**
   * Create a LibraryClasspath to access a subset of the catalog
   */
  public LibraryClasspath createLibraryClasspath()
  { return new LibraryClasspathImpl();
  }

  public Library findLibrary(String fileName)
  {
    Library library
      =getLibrary
        (new File(codebaseRootPath+File.separator+fileName)
          .getAbsolutePath()
        );
    return library;

  }

  private Library getLibrary(String fullPath)
  { 
    for (Library library: codebaseLibraries)
    { 
      if (library.path.equals(fullPath))
      { return library;
      }
    }
    return null;
  }

  /**
   * Load catalog data into memory
   */
  private void loadCatalog()
  { 
    try
    { discoverLibraries();
    }
    catch (IOException x)
    { x.printStackTrace();
    }
  }

  /**
   * Discovers all libraries usable by this catalog
   */
  private void discoverLibraries()
    throws IOException
  { 
    File[] libs
      =new File(codebaseRootPath)
        .listFiles
          (new FilenameFilter()
          {
            public boolean accept(File dir,String name)
            { 
              return name.endsWith(".jar")
                || name.endsWith(".dll")
                || name.endsWith(".so")
                ;
            }
          }
          );

    codebaseLibraries.clear();

    if (libs!=null)
    {
      for (int i=0;i<libs.length;i++)
      { catalogLibrary(libs[i]);
  
      }
    }

  }

  private void catalogLibrary(File file)
    throws IOException
  {
    Library lib;
    if (file.getName().endsWith(".jar"))
    { lib=new JarLibrary(file);
    }
    else if (file.getName().endsWith(".dll")
            || file.getName().endsWith(".so")
            )
    { lib=new NativeLibrary(file);
    }
    else
    { lib=new FileLibrary(file);
    }
    codebaseLibraries.add(lib);
  }

  /**
   * Implementation of LibraryClasspath- uses a subset of the LibraryCatalog
   *   to load classes and resources.
   */
  class LibraryClasspathImpl
    implements LibraryClasspath
  {
    private final ClassLog log
      =ClassLog.getInstance(LibraryClasspathImpl.class);
    
    private final ListMap<String,Resource> resources
      =new ListMap<String,Resource>();

    
    private final ArrayList<Library> classpathLibraries
      =new ArrayList<Library>();

    private boolean debug;
    
    public void setDebug(boolean debug)
    { this.debug=debug;
    }
    
    public void release()
    {
      if (debug)
      { log.fine("Releasing...");
      }
      
      for (Library library: classpathLibraries)
      {
        try
        { 
          if (debug)
          { log.fine("Closing "+library.name);
          }
          library.close();
        }
        catch (IOException x)
        { }
      }
      classpathLibraries.clear();
      resources.clear();
    }
    
    public byte[] loadData(String path)
      throws IOException
    {
      Resource resource=resources.getOne(path);
      if (resource==null)
      { throw new IOException("Not found: "+path);
      }
      if (debug)
      { 
        log.fine
          ("Loading data from  "+resource.library.path+"!"+resource.name);
      }
      return resource.getData();
    }

    @Override
    public URL getResource(String path)
      throws IOException
    {
      Resource resource=resources.getOne(path);
      if (resource==null)
      { return null;
      }
      if (debug)
      {
        log.fine
          ("Returning reference to resource "
          +resource.library.path+"!"+resource.name
          );
      }
      return resource.getResource();
    }
    
    @Override
    public Enumeration<URL> getResources(String path)
      throws IOException
    {
      List<Resource> resourceList=resources.get(path);
      List<URL> urlList=new LinkedList<URL>();
      if (resourceList!=null)
      {
        for (Resource resource:resourceList)
        { urlList.add(resource.getResource());
        }
      }
      return new IteratorEnumeration<URL>(urlList.iterator());
    }

    /**
     * Adds the libraries which contain the specified module 
     *   and its dependents to the set of libraries available
     *   to the classloader.
     */
    public void addModule(String name)
      throws IOException
    { 
      LinkedList<Library> libraries
        =new LinkedList<Library>();
      for (Library library: codebaseLibraries)
      {
        if (library.isModule(name))
        { libraries.add(library);
        }
      }
      
      if (libraries.size()==0)
      { throw new IOException("Module not found: "+name);
      }
      
      if (debug)
      { log.fine("Adding module "+name+" to classpath");
      }
      addLibrary(libraries.get(0));
    }
    
    public void addAllModules()
      throws IOException
    {

      for (Library library: codebaseLibraries)
      { addLibrary(getLatestVersion(library));
      }
       
    }

    public Library getLatestVersion(Library library)
    { return library;
    }
    
    public void addLibrary(String path)
      throws IOException
    { 
      boolean found=false;
      for (Library library: codebaseLibraries)
      { 
        // Use versioning logic to find the best
        //   library in the future
        if (library.path.equals(path))
        { 
          addLibrary(library);
          found=true;
          break;
        }
      }
      if (!found)
      { throw new IOException("Not found: "+path);
      }
    }

    private void addLibrary(Library library)
      throws IOException
    {
      if (classpathLibraries.contains(library))
      { return;
      }
      
      if (debug)
      { log.fine("Adding library "+library.path+" to classpath");
      }
      
      library.open();
      classpathLibraries.add(library);
      
      for (Entry<String, Resource> entry:library.resources.entrySet())
      { resources.add(entry.getKey(),entry.getValue());
      }
      
      String[] dependencies
        =library.getLibraryDependencies();

      if (dependencies!=null)
      {
        for (int i=0;i<dependencies.length;i++)
        { 
          Library depends=findLibrary(dependencies[i]);
          if (depends!=null)
          { addLibrary(depends);
          }
          else
          { 
            throw new IOException
              ("Unsatisified dependency "+dependencies[i]+" loading "+library.path);
          }
        }
      }
            
    }

    public void resolveLibrariesForResource(String resourcePath)
      throws IOException
    { 
      List<Library> libraries=new LinkedList<Library>();

      Iterator<Library> it=codebaseLibraries.iterator();
      while (it.hasNext())
      { 
        Library library=it.next();

        if (library.resources.get(resourcePath)!=null)
        { libraries.add(library);
        }
      }

      if (libraries.size()==0)
      { throw new IOException("Not found: "+resourcePath);
      }
      
      addLibrary(libraries.get(0));
      
    }
    
    public String findNativeLibrary(String name)
    {

      for (Library library: codebaseLibraries)
      { 
        if ((library instanceof NativeLibrary)
            && library.name.equals(name)
           )
        { return library.path;
        }
      }
      return null;
      
    }


  }

}

class CatalogEntry
{
  String name;
  Library library;
  
}

abstract class Library
{
  String path;
  String name;
  long lastModified;
  HashMap<String,Resource> resources
    =new HashMap<String,Resource>();

  public Library(File file)
    throws IOException
  { 
    path=file.getAbsolutePath();
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

}

class JarLibrary
  extends Library
{

  int openCount=0;
  JarFile jarFile;
  Manifest manifest;

  public JarLibrary(File file)
    throws IOException
  { 
    super(file);

    name=file.getName();
    if (name.endsWith(".jar"))
    { name=name.substring(0,name.length()-4);
    }
  }

  @Override
  public void catalogResources()
    throws IOException
  {
    
    jarFile=
      new JarFile(path);
    try
    {
      Enumeration<JarEntry> entries=jarFile.entries();
  
      while (entries.hasMoreElements())
      {
        JarEntry jarEntry
          =entries.nextElement();
        JarResource resource
          =new JarResource();
        resource.entry=jarEntry;
        resource.name=jarEntry.getName();
        if (resource.name.endsWith("/"))
        { resource.name=resource.name.substring(0,resource.name.length()-1);
        }
        resource.library=this;
        resources.put(resource.name,resource);
      }
    }
    finally
    { 
      jarFile.close();
      jarFile=null;
    }
    
   
  }

  @Override
  public synchronized void open()
    throws IOException
  { 
    if (openCount==0)
    { 
      jarFile=new JarFile(path);
      readManifest();
    }
    openCount++;
  }

  @Override
  public synchronized void close()
    throws IOException
  {
    openCount--;
    if (openCount==0)
    { jarFile.close();
    }
  }

  @Override
  public synchronized void forceClose()
    throws IOException
  { 
    if (jarFile!=null)
    { jarFile.close();
    }
  }
  
  public byte[] getData(JarEntry entry)
    throws IOException
  {
    BufferedInputStream in=null;
    try
    {
      in = new BufferedInputStream(jarFile.getInputStream(entry));

      byte[] data = new byte[(int) entry.getSize()];
      in.read(data);
      in.close();
      return data;
    }
    catch (IOException x)
    { 
      if (in!=null)
      {
        try
        { in.close();
        }
        catch (IOException y)
        { }
      }
      throw x;
    }
  }

  private void readManifest()
    throws IOException
  { manifest=jarFile.getManifest();
  }

  /**
   * Return the list of libraries that this library depends on
   */
  @Override
  public String[] getLibraryDependencies()
  {
    if (manifest==null)
    { return null;
    }
    
    String classpath
      =manifest.getMainAttributes().getValue(Attributes.Name.CLASS_PATH);
    if (classpath!=null)
    { 
      String[] classpathArray
        =StringUtil.tokenize(classpath," ");
      return classpathArray;
    }
    return null;
  }
  

}

class NativeLibrary
  extends Library
{


  public NativeLibrary(File file)
    throws IOException
  { 
    super(file);
    name=file.getName();
    if (name.endsWith(".dll"))
    { name=name.substring(0,name.length()-4);
    }
    else if (name.endsWith(".so"))
    { name=name.substring(0,name.length()-3);
    }
  }

  @Override
  public void catalogResources()
    throws IOException
  {
  }

  @Override
  public synchronized void open()
    throws IOException
  { 
  }

  @Override
  public synchronized void close()
    throws IOException
  {
  }

  @Override
  public synchronized void forceClose()
    throws IOException
  { 
  }
  
  @Override
  public String[] getLibraryDependencies()
  { return null;
  }

}

class FileLibrary
  extends Library
{
  public FileLibrary(File file)
    throws IOException
  { super(file);
  }

  @Override
  public void catalogResources()
  {
  }

  @Override
  public void open()
  {
  }

  @Override
  public void close()
  {
  }

  @Override
  public void forceClose()
  { 
  }
  
  @Override
  public String[] getLibraryDependencies()
  { return null;
  }
}

abstract class Resource
{
  Library library;
  String name;

  public abstract byte[] getData()
    throws IOException;

  public abstract URL getResource()
    throws IOException;
}

class JarResource
  extends Resource
{
  JarEntry entry;
  
  @Override
  public byte[] getData()
    throws IOException
  { return ((JarLibrary) library).getData(entry);
  }

  @Override
  public URL getResource()
    throws IOException
  { return new URL("jar:file:///"+library.path.replace('\\','/')+"!/"+name);
  }
}

class FileResource
  extends Resource
{
  File file;

  @Override
  public byte[] getData()
  { return null;
  }

  @Override
  public URL getResource()
    throws IOException
  { return new URL("file:/"+library.path+"/"+name);
  }

}

class ApplicationInfo
{
  
}

