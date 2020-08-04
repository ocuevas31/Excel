import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

public class join3
{
  private static int cuentaColumnas;
  private static int cuentaColumnas2;
  private static String filelog;
  private static String filelog2;
  
  public static void main(String[] args)
    throws IOException
  {
    String file1 = args[0];
    System.out.println("args[0] " + args[0]);
    String file2 = args[1];
    System.out.println("args[1] " + args[1]);
    String fileout = args[2];
    System.out.println("args[2] " + args[2]);
    
    int key1 = Integer.parseInt(args[3]);
    System.out.println("args[3] " + args[3]);
    int Key2 = Integer.parseInt(args[4]);
    System.out.println("args[4] " + args[4]);
    
    String posicion1 = args[5];
    System.out.println("args[5] " + args[5]);
  
    String posicion2;
    if (args[6].equals("_")) {
      posicion2 = "";
    } else {
      posicion2 = args[6];
    }
    System.out.println("args[6] " + args[6]);
    
    String separator = args[7];
    System.out.println("args[7] " + args[7]);
    String order = args[8];
    System.out.println("args[8] " + args[8]);
    
    String[] parts = args[9].split(";");
    String part1 = parts[0];
    String part2 = parts[1];
    String part3 = parts[2];
    boolean write_unmatched = Boolean.parseBoolean(part1);
    System.out.println("args[9.1] " + part1);
    String encoding = "ISO-8859-1";
    cuentaColumnas = Integer.parseInt(part2);
    System.out.println("args[9.2] " + part2);
    cuentaColumnas2 = Integer.parseInt(part3);
    System.out.println("args[9.3] " + part3);
    filelog = file1.substring(0, file1.length() - 4) + "_DISCARTED.log";
    System.out.println("args[10.1] " + filelog);
    filelog2 = file2.substring(0, file1.length() - 4) + "_DISCARTED.log";
    System.out.println("args[10.2] " + filelog2);
    
    HashMap<String, ArrayList<String>> file1map = new HashMap();
    HashMap<String, ArrayList<String>> file2map = new HashMap();
    ArrayList<ArrayList<String>> arrayFileOut = new ArrayList();
    
    file1map = readFile(file1, key1, separator, encoding, posicion1);
    file2map = readFile2(file2, Key2, separator, encoding, posicion2);
    arrayFileOut = join(file1map, file2map, order, write_unmatched);
    writeFile(arrayFileOut, separator, fileout, encoding);
  }
  
  private static ArrayList<ArrayList<String>> join(HashMap<String, ArrayList<String>> file1map, HashMap<String, ArrayList<String>> file2map, String order, boolean write_unmatched)
  {
    ArrayList<String> line = new ArrayList();
    Iterator<?> it = file1map.entrySet().iterator();
    ArrayList<ArrayList<String>> lineas = new ArrayList();
    while (it.hasNext())
    {
      Map.Entry e = (Map.Entry)it.next();
      
      ArrayList<String> arr = (ArrayList)e.getValue();
      if (file2map.get(((String)e.getKey()).split(";;")[0]) != null) {
        line = order(arr, (ArrayList)file2map.get(((String)e.getKey()).split(";;")[0]), order, false);
      }
      if ((file2map.get(((String)e.getKey()).split(";;")[0]) == null) && (write_unmatched)) {
        line = order(arr, (ArrayList)file2map.get(((String)e.getKey()).split(";;")[0]), order, 
          write_unmatched);
      }
      if ((line != null) && (line.size() > 0)) {
        lineas.add(line);
      }
    }
    return lineas;
  }
  
  private static ArrayList<String> order(ArrayList<String> arr1, ArrayList<String> arr2, String order, boolean write_unmatched)
  {
    ArrayList<String> aux = new ArrayList();
    
    String emptyString = "";
    for (int k = 0; k < 400; k++) {
      emptyString = emptyString + " ";
    }
    for (int i = 0; i < order.split(",").length; i++) {
      if (write_unmatched)
      {
        if (order.split(",")[i].startsWith("x")) {
          aux.add((String)arr1.get(Integer.parseInt(order.split(",")[i]
            .split("-")[1])));
        } else {
          aux.add(emptyString);
        }
      }
      else if (order.split(",")[i].startsWith("x")) {
        aux.add((String)arr1.get(Integer.parseInt(order.split(",")[i]
          .split("-")[1])));
      } else if (order.split(",")[i].startsWith("y")) {
        aux.add((String)arr2.get(Integer.parseInt(order.split(",")[i]
          .split("-")[1])));
      }
    }
    return aux;
  }
  
  private static HashMap<String, ArrayList<String>> readFile(String file, int key, String separator, String encoding, String posicion)
    throws IOException
  {
    HashMap<String, ArrayList<String>> mapAux = new HashMap();
    ArrayList<String> arrAux = new ArrayList();
    
    FileOutputStream fos = new FileOutputStream(filelog);
    OutputStreamWriter osr = new OutputStreamWriter(fos, encoding);
    BufferedWriter bw = new BufferedWriter(osr);
    
    FileInputStream fis = new FileInputStream(file);
    InputStreamReader isr = new InputStreamReader(fis, encoding);
    
    BufferedReader b = new BufferedReader(isr);
    String cadena;
    while ((cadena = b.readLine()) != null)
    {
    
      int count = StringUtils.countMatches(cadena, ";");
      if (count != cuentaColumnas)
      {
        bw.write(cadena + "\n");
      }
      else
      {
        for (int i = 0; i < cadena.split(separator).length; i++) {
          arrAux.add(cadena.split(separator)[i]);
        }
        ArrayList<String> aux = new ArrayList();
        aux.addAll(arrAux);
        
        String claveaux = cadena.split(separator)[key];
        if ((posicion != null) && (!posicion.isEmpty())) {
          for (int j = 0; j < posicion.split(",").length; j++) {
            claveaux = claveaux + ";;" + cadena.split(separator)[Integer.parseInt(posicion.split(",")[j])];
          }
        }
        mapAux.put(claveaux, aux);
        arrAux.clear();
      }
    }
    b.close();
    bw.close();
    
    return mapAux;
  }
  
  private static HashMap<String, ArrayList<String>> readFile2(String file, int key, String separator, String encoding, String posicion)
    throws IOException
  {
    HashMap<String, ArrayList<String>> mapAux = new HashMap();
    ArrayList<String> arrAux = new ArrayList();
    FileInputStream fis = new FileInputStream(file);
    InputStreamReader isr = new InputStreamReader(fis, encoding);
    
    FileOutputStream fos = new FileOutputStream(filelog2);
    OutputStreamWriter osr = new OutputStreamWriter(fos, encoding);
    BufferedWriter bw = new BufferedWriter(osr);
    
    BufferedReader b = new BufferedReader(isr);
    String cadena;
    while ((cadena = b.readLine()) != null)
    {
    
      int count = StringUtils.countMatches(cadena, ";");
      if ((count != cuentaColumnas2) || (cadena.length() != 412))
      {
        bw.write(cadena + "\n");
      }
      else
      {
        for (int i = 0; i < cadena.split(separator).length; i++) {
          arrAux.add(cadena.split(separator)[i]);
        }
        ArrayList<String> aux = new ArrayList();
        aux.addAll(arrAux);
        
        String claveaux = cadena.split(separator)[key];
        if ((posicion != null) && (!posicion.isEmpty())) {
          for (int j = 0; j < posicion.split(",").length; j++) {
            claveaux = claveaux + ";;" + cadena.split(separator)[Integer.parseInt(posicion.split(",")[j])];
          }
        }
        mapAux.put(claveaux, aux);
        arrAux.clear();
      }
    }
    b.close();
    bw.close();
    
    return mapAux;
  }
  
  private static void writeFile(ArrayList<ArrayList<String>> lines, String separator, String fileout, String encoding)
  {
    try
    {
      FileOutputStream fis = new FileOutputStream(fileout);
      OutputStreamWriter isr = new OutputStreamWriter(fis, encoding);
      BufferedWriter bw = new BufferedWriter(isr);
      
      ArrayList<String> line = new ArrayList();
      for (int i = 0; i < lines.size(); i++)
      {
        line = (ArrayList)lines.get(i);
        
        String cadena = new String();
        for (int j = 0; j < line.size(); j++) {
          cadena = cadena + (String)line.get(j) + separator;
        }
        cadena = cadena + "\n";
        
        bw.write(cadena);
      }
      bw.close();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }
}
