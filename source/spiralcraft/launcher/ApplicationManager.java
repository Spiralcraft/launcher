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


import spiralcraft.lang.BindException;
//import spiralcraft.log.ClassLogger;

import spiralcraft.util.ArrayUtil;
import spiralcraft.vfs.Resolver;
import spiralcraft.vfs.Resource;
import spiralcraft.vfs.UnresolvableURIException;

import spiralcraft.data.persist.AbstractXmlObject;
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
    _catalog=
      new LibraryCatalog
        (new File(_codebase,"lib")
        );
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
    if (args.length==0)
    { 
      URI applicationURI=findDefaultEnvironment();
      if (applicationURI==null)
      { 
        if (searchPath.length>0)
        {
          System.err.println("Could not find default application environment "
            +" "+ArrayUtil.format(searchPath,"\r\n ,","[","]")
            );
          System.err.println(" ");
        }
        launch(URI.create("class:/spiralcraft/launcher/builtins/help.env.xml"),args);
      }
      else
      { launch(applicationURI,args);
      }
      
    }
    else
    {

      URI applicationURI=findEnvironment(args[0],".env.xml");
      if (applicationURI==null)
      { 
        // Show environments in-scope
        throw new IllegalArgumentException
          ("Unknown application environment '"+args[0]+"', searched:\r\n  "
          +ArrayUtil.format(searchPath,"\r\n  ,","[","]")
          );
      }

      args=ArrayUtil.truncateBefore(args,1);
      launch(applicationURI,args);
    }
  }

  private void launch(URI applicationURI,String[] args)
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
    catch (BindException x)
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


