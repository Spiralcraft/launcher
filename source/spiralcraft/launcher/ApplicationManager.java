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


//import spiralcraft.log.ClassLogger;

import spiralcraft.util.ArrayUtil;
import spiralcraft.vfs.Container;
import spiralcraft.vfs.Resolver;
import spiralcraft.vfs.Resource;
import spiralcraft.vfs.UnresolvableURIException;

import spiralcraft.bundle.Library;
import spiralcraft.common.ContextualException;
import spiralcraft.data.persist.AbstractXmlObject;
import spiralcraft.cli.BeanArguments;
import spiralcraft.exec.ExecutionContext;

import java.io.File;
import java.io.IOException;

import java.net.URI;
//import java.util.logging.Logger;

/**
 * <p>Controls the execution of applications via one or more
 *   ApplicationEnvironments.
 * </p>
 * 
 * <p>An ApplicationEnvironment defines the code modules (.jar or directory trees)
 *   and an entry point necessary to run an application.
 * </p>
 *
 * <p>
 * ApplicationEnvironments are stored in Resources identified by a URI, which
 *   is provided to the exec() method.
 * </p>
 *
 * <p>
 * If the URI is relative, the ApplicationManager will search for the
 *   Resource according to the following rules:
 *
 *   <ol>
 *     <li>"user.dir" Java system property
 *     </li>
 *     <li>User environment path (<user-home>/.spiralcraft/env)
 *     </li>
 *     <li>System environment path (<spiralcraft-home>/env)
 *     </li>
 *   </ol>
 * </p>
 * 
 * @author mike
 */
public class ApplicationManager
{

//  private static final Logger log
//    =ClassLogger.getInstance(ApplicationManager.class);
  

  private URI[] searchPath;
    
  private final File _codebase;
      
  private final LibraryCatalog _catalog;
  
//  private final String _userId;

  private final URI _codebaseEnvironmentURI;

  private String title;

  private final URI _userHomeEnvironmentURI
    =new File(System.getProperty("user.home"))
      .toURI().resolve(".spiralcraft/env/");

//  private int _nextEnvironmentId=0;

  private boolean debug=false;
  

  public ApplicationManager(String userId,File codebase)
  { 
//    _userId=userId;
    _codebase=codebase;
    _codebaseEnvironmentURI=_codebase.toURI().resolve("env/");
    Library library=resolveLibrary();
    if (library!=null)
    { Library.set(library);
    }
    _catalog=
      new LibraryCatalog
        (new File(_codebase,"lib")
        );
    
  }

  
  /**
   * Map the contents of all packages contained in context:/packages into
   *   the contextResourceMap
   * 
   * @param contextResourceMap
   */
  private Library resolveLibrary()
  {
    try
    {
      Resource libraryResource
        =Resolver.getInstance().resolve(_codebase.toURI().resolve("packages/"));
      Container container=libraryResource.asContainer();
      if (container!=null)
      { return new Library(container);
      }
    }
    catch (UnresolvableURIException x)
    { x.printStackTrace();
    }
    return null;
  }    
  
  public void setViewTitle(String title)
  { this.title=title;
  }
  
  public String getViewTitle()
  { return title;
  }
  
  public void setDebug(boolean val)
  { debug=val;
  }
  
  public void shutdown()
  { _catalog.close();
  }
  
  public LibraryCatalog getLibraryCatalog()
  { return _catalog;
  }

  public void exec(String[] args)
    throws LaunchException
  { 
    int commandPos=0;
    String[] envArgs=new String[0];
    if (args.length>0 && args[0].startsWith("-"))
    {
      // Skip all the options until we find "--"
      while (commandPos++<args.length)
      { 
        if (args[commandPos-1].equals("--"))
        { 
          envArgs=ArrayUtil.truncate(args,commandPos-1);
          break;
        }
      }
    }
      
      
      
    URI applicationURI=null;
    if (commandPos<args.length)
    {
      applicationURI=findEnvironment(args[commandPos],".env.xml");
      if (applicationURI==null)
      { 
        // Show environments in-scope
        throw new IllegalArgumentException
          ("Unknown application environment '"+args[commandPos]+"', searched:\r\n  "
          +ArrayUtil.format(searchPath,"\r\n  ,","[","]")
          );
      }
      args=ArrayUtil.truncateBefore(args,commandPos+1);
    }
    else
    { 
      applicationURI=findDefaultEnvironment();
      envArgs=args;
      args=new String[0];
      if (applicationURI==null)
      { 
        if (searchPath.length>0)
        {
          System.err.println("Could not find default application environment "
            +" "+ArrayUtil.format(searchPath,"\r\n ,","[","]")
            );
          System.err.println(" ");
        }
        launch(URI.create("class:/spiralcraft/launcher/builtins/help.env.xml"),new String[0],args);
        return;
      }
    }

    launch(applicationURI,envArgs,args);
  }

  private void launch(URI applicationURI,String[] envArgs,String[] args)
    throws LaunchException
  {
    try
    {
      AbstractXmlObject<ApplicationEnvironment,?> environmentRef
      =AbstractXmlObject.<ApplicationEnvironment>activate
      (null
        ,applicationURI
        ,null
      );


      ApplicationEnvironment environment=environmentRef.get();
      new BeanArguments<ApplicationEnvironment>(environment).process(envArgs);
      environment.resolve(this);
      
      try
      { environment.exec(args);
      }
      catch (LaunchTargetException x)
      { 
        throw new LaunchException
          ("Error launching "+applicationURI+": "+x.getMessage()
          ,x.getCause()
          );
      }
    }
    catch (ContextualException x)
    { throw new LaunchException("Error binding "+applicationURI,x);
    }
    
  }
  
  
  private URI findDefaultEnvironment()
  {
    searchPath=new URI[0];
    String defaultEnvironment
      =System.getProperty("spiralcraft.launcher.default.env");
    
    if (defaultEnvironment==null)
    { defaultEnvironment="spiralcraft";
    }
    else if (defaultEnvironment.trim().equals(""))
    { 
      return null;
    }
    
    URI nameURI=URI.create(defaultEnvironment+".env.xml");
    ExecutionContext context=ExecutionContext.getInstance();

    URI searchURI;
    if (context==null)
    {
      searchURI=new File(System.getProperty("user.dir")).toURI()
        .resolve(nameURI);
    }
    else
    { searchURI=context.canonicalize(nameURI);
    }
    
    
    if (isEnvironment(searchURI))
    { return searchURI;
    }
    searchPath=ArrayUtil.append(searchPath,searchURI);
    return null;
  }
    
  /**
   * Search for the named environment, in order of priority:
   *   
   *   1. An absolute resource if specified, whether or not it exists
   *   2. A resource relative to the user's "current directory" (user.dir)
   *   3. Launcher built-ins
   *   4. Codebase built-ins
   *   5. codebase environment path 
   *   6. user home directory/.spiralcraft/env/* 
   *
   */
  private URI findEnvironment(String name,String suffix)
  {
    URI nameURI=URI.create(name+suffix);
    URI searchURI=null;
    
    
    if (nameURI.isAbsolute() && isEnvironment(nameURI))
    { 
      searchPath=new URI[] {nameURI};
      return nameURI;
    }
    else
    { searchPath=new URI[] {};
    }

    searchURI=new File(System.getProperty("user.dir")).toURI().resolve(nameURI);
    if (isEnvironment(searchURI))
    { return searchURI;
    }
    searchPath=ArrayUtil.append(searchPath,searchURI);

    searchURI=URI.create
      ("class:/spiralcraft/launcher/builtins/").resolve(nameURI);
    if (isEnvironment(searchURI))
    { return searchURI;
    }
    searchPath=ArrayUtil.append(searchPath,searchURI);

    searchURI=URI.create
      ("sclib:/META-INF/spiralcraft.env/").resolve(nameURI);
    if (isEnvironment(searchURI))
    { return searchURI;
    }
    searchPath=ArrayUtil.append(searchPath,searchURI);
    
    searchURI=_codebaseEnvironmentURI.resolve(nameURI);
    if (isEnvironment(searchURI))
    { return searchURI;
    }
    searchPath=ArrayUtil.append(searchPath,searchURI);
    

    searchURI=_userHomeEnvironmentURI.resolve(nameURI);
    if (isEnvironment(searchURI))
    { return searchURI;
    }
    searchPath=ArrayUtil.append(searchPath,searchURI);


    return null;
  }

  private boolean isEnvironment(URI uri)
  {
    if (debug)
    { System.err.println("Searching for "+uri);
    }
    
    try
    { 
      Resource resource = Resolver.getInstance().resolve(uri);
      if (resource.exists())
      { 
        if (debug)
        { System.err.println("Found "+uri);
        }
        return true;
      }
      else
      { return false;
      }
    }
    catch (UnresolvableURIException x)
    { System.err.println(x.toString());
    }
    catch (IOException x)
    { System.err.println(x.toString());
    }
    return false;
  }
}


