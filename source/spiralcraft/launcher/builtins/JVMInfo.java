//
// Copyright (c) 2009 Michael Toth
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
package spiralcraft.launcher.builtins;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.TreeMap;

import spiralcraft.launcher.ApplicationEnvironment;
import spiralcraft.launcher.LaunchException;

/**
 * Info about running JVM
 * 
 * @author mike
 *
 */
public class JVMInfo
  extends ApplicationEnvironment
{

  @Override
  public void exec(String[] args)
    throws LaunchException
  { 
    dumpRuntime(System.out);
    System.out.println(" ");
    dumpProperties(System.out);
    System.out.println(" ");
    dumpEnvironment(System.out);
  }
  
  public void dumpRuntime(PrintStream out)
  {
    out.println("Runtime Environment");
    out.println(" ");
    out.println("    System.currentTimeMillis() = "
      +System.currentTimeMillis());
    out.println("    System.currentTimeMillis() = "
      +new Date(System.currentTimeMillis()));
    out.println("    System.nanoTime() = "
      +System.nanoTime());
    
    out.println("    System.console() = "
      +System.console());
    out.println("    System.getSecurityManager() = "
      +System.getSecurityManager());
    try
    {
      out.println("    System.inheritedChannel() = "
        +System.inheritedChannel()
        );
    }
    catch (IOException x)
    {
      out.println("    System.inheritedChannel() = "
        +"(threw "+x+")"
        );
    }
    
    out.println(" ");
    
    Runtime runtime=Runtime.getRuntime();
    out.println("    Runtime.availableProcessors() = "
      +runtime.availableProcessors());
    out.println("    Runtime.freeMemory() = "
      +runtime.freeMemory());
    out.println("    Runtime.maxMemory() = "
      +runtime.maxMemory());
    out.println("    Runtime.totalMemory() = "
      +runtime.totalMemory());
    
    out.println(" ");
    out.println("    Thread.currentThread() = "
      +Thread.currentThread());
    out.println("    Thread.currentThread().getContextClassLoader() = "
      +Thread.currentThread().getContextClassLoader());
    out.println("    Thread.currentThread().getThreadGroup() = "
      +Thread.currentThread().getThreadGroup());
    
    out.println(" ");
    out.println("    Locale.getDefault() = "
        +Locale.getDefault());
    out.println("    TimeZone.getDefault() = "
      +TimeZone.getDefault());
    
  }
  
  public void dumpEnvironment(PrintStream out)
  { 
    Map<String,String> env=System.getenv();
    TreeMap<String,String> map=new TreeMap<String,String>();
    
    for (String key:env.keySet())
    { map.put(key,env.get(key));
    }
    
    out.println("System.getenv() - Host Environment");
    out.println(" ");
    for (Map.Entry<String,String> entry: map.entrySet())
    { 
      out.println("    "+entry.getKey()+" = "+entry.getValue());
    }
  }
  
  
  public void dumpProperties(PrintStream out)
  { 
    Properties props=System.getProperties();
    TreeMap<String,String> map=new TreeMap<String,String>();
    
    for (Object key:props.keySet())
    { map.put(key.toString(),props.getProperty(key.toString()));
    }
    
    out.println("System.getProperies() - System Properties");
    out.println(" ");
    for (Map.Entry<String,String> entry: map.entrySet())
    { 
      out.println("    "+entry.getKey()+" = "+entry.getValue());
    }
    
  }
  
}
