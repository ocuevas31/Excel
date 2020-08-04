
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.sl.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.tika.Tika;
//import org.apache.tika.Tika;



/*PARAMETROS   
 *  ruta al fichero del que obtener las SDIs (igual mejor sacarlos segun entorno)
 *  fila donde estan los encabezados de las columans en el excel, se asume que la siguiente fila ya tiene la primera SDI a cargar
 *  Texto que representa la columna donde esta el texto del PL
 * */


/*La fila 10 en excel (la 9 en codigo) tiene el nombre de las columnas y tiene que tener una llamada PL que es donde se encontrara la llamada a la funcion VB loadSDI(...*/
/*Si el nombre de la Cparty es vacio -> no carga mas SDIs*/
/*La ruta de la que obtener el fichero es el primer parametro
 * la columna donde esta la cparty  es el segundo params 0,1,2.... A,B,C....*/



public class Carga_SDI {

	// private static final Logger logger = Logger.getLogger(Carga_SDI.class);
	// //import org.apache.log4j.Logger;

	private static final String MS_EXCEL = "ms-excel";
	private static int FILAINICIAL = 9; // fila que contiene la primera SDI a
										// cargar del excel(se pasa por
										// parametro)
	private static int FILAENCABEZADOS = 8;
	public static String TEXTOCOLUMNAPL = "PL";

	public static String rutaFichero = null;
	public static final String VACIA = "";
	static final String[] parametros = { "Ruta fich", "Maxfiles" };
	static String USER;
	
	static private long cod;
	
	
	/*JOB ID*/
	public static String FLD_JOB_ID = null;
	
	
	/*
	 * 
	 * 
	 * 
	 * crear constructor y pillar los argumentos
	 * 
	 * 
	 */
	
	static public String generateJOBID()
	  {
	    char[] chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();
	    StringBuilder sb = new StringBuilder();
	    Random random = new Random();
	    for (int i = 0; i < 16; i++)
	    {
	      char c = chars[random.nextInt(chars.length)];
	      sb.append(c);
	    }
	    String output = sb.toString();
	    
	    return output;
	  }
	
	public static void main(String[] args) {

		System.out.println("INI");

		argumentos(args);

		String nombreArchivo = getFileName();
		String ruta = getPath();
		String rutaArchivo = ruta + nombreArchivo;

		System.out.println("Ruta Fich: " + rutaArchivo);

		System.out.println("Fila ini de donde obtener filas PL: " + FILAINICIAL);

		System.out.println("Fila donde estan los encabezados: " + FILAENCABEZADOS);
		
		
		
		
		/*Hay un crearJOB y un cerrarJOB en la clase DDBB (ConDB) que se puede usar en lugar del cod para localizar las filas de la RLT1 a mostrar en el reporte*/
		

		try {

			FileInputStream file = new FileInputStream(new File(rutaArchivo));

			esExcel(rutaArchivo);

			// leer archivo excel
			XSSFWorkbook worbook = new XSSFWorkbook(file);
			// obtener la hoja que se va leer
			XSSFSheet sheet = worbook.getSheetAt(0);

			
			
			/*
			 * Hay que almacenar el codigo de tiempo al ppio para ponerles a
			 * todos los pl el mismo
			 */
			cod = System.currentTimeMillis();

			ArrayList<String> l=null;
			
			
			try
			{
				l = recuperarLlamadasPL(sheet);

			}catch (Exception e)
			{
				System.out.println(e);
				
			}
			
			System.out.println("!Despues de recuperarLLamadasPL y antes de genrerar Reporte");
			System.out.println("El numero de SDIs a cargar es :" + l.size());

			//System.in.read();
			//System.out.println(l);

			DDBB db = new DDBB();

			db.ObtenerCredenciales();

			Connection c = db.ObtenerConexion();
			
			/*nuevo con job ID*/
			FLD_JOB_ID = USER+cod;//generateJOBID();
		    long inicio = db.crearJOB(FLD_JOB_ID, "CARGA_SDI", c);
		    System.out.println("Se crea JOB");
		    
		    
			
			
			/*fin nuevo JOB ID*/
			
			
			
			//crear JOB

			realizarLlamadasPL(l, c);
			
			
			//cerrar JOB
			
			db.cerrarJOB(FLD_JOB_ID, "CARGA_SDI", inicio, c);

			generarReporte(c);

			
			
			DDBB.closePooledConnections();
			
			c.close();
			

		} catch (Exception e) {
			System.out.println(e.getMessage());

		}

		System.out.println("FIN");
	}
	

	

	/**
	 * Devuelve la ruta al fichero de excel
	 */
	private static String getPath() {

		return rutaFichero == null ? "/tmp/os/" : rutaFichero;

	}

	/**
	 * Devuelve el nombre del fichero de excel
	 */
	private static String getFileName() {

		return "Plantilla_CargaSDIs.xlsm";
	}

	public static boolean esTextoPL(String cte, String delexcel) {
		return cte.compareToIgnoreCase(delexcel) == 0;
	}

	public static int buscarColumnaPL(XSSFSheet sheet) {
		int res = 0;

		// Buscar la columna en la fila 9 que contiene el texto que indica que
		// esa es la columna de carga del PL

		Row row = sheet.getRow(FILAENCABEZADOS);

		Iterator<Cell> cellIterator = row.cellIterator();
		Cell cell = null; // se recorre cada fila (sus celdas) int j=0;
		while (cellIterator.hasNext()) {

			cell = cellIterator.next();
			// System.out.println(cell.getStringCellValue()+" ----");
			if (esTextoPL(TEXTOCOLUMNAPL, cell.getStringCellValue()))
				return res + 2;
			res++;

		}

		return res;
	}

	/* para ver si hay cparty o no */
	private static boolean lineaVacia(String pl) {
		int posComi1 = pl.indexOf('\'');
		int posComi2 = pl.indexOf('\'', posComi1 + 1);
		String cparty = pl.substring(posComi1 + 1, posComi2);

		return cparty.length() == 0;

	}

	/*
	 * dada una linea de carga del pl elimina el comit y añade al usuario el
	 * codigo de fecha actual para despues poder localizar los datos
	 * correctamente Devuelve "" si ya no hay mas datos a cargar...
	 * 
	 * ahora se pone en el job_id
	 */
	private static String tratarTextoPL(String pl) {

		String nuevoPl = null;

		try {

			if (lineaVacia(pl))
				return VACIA;

			int posLastComa = pl.lastIndexOf(',');
			int posLastX = pl.lastIndexOf('\'');
			String user = pl.substring(posLastComa, posLastX);
			USER = user.substring(2);
		
			//buscamos el ultimo '' para meter alli el job_id=user+cod
			
			int posJob=pl.lastIndexOf("''");
			String cadiz=pl.substring(0,posJob+1);
			String cadde=pl.substring(posJob+1,pl.length()-";Commit;".length());
			
			nuevoPl=cadiz+USER+cod+cadde;
			
			//nuevoPl = pl.substring(0, posLastComa) + user + cod + "')";

		} catch (Exception e) {
			nuevoPl = VACIA;

		}
		return nuevoPl;

	}

	/*
	 * para obtener el numero de columna dada la columna en formato excel
	 * A,B,C....
	 */
	public static int getExcelColumnNumber(String column) {
		int result = 0;
		for (int i = 0; i < column.length(); i++) {
			result *= 26;
			result += column.charAt(i) - 'A' + 1;
		}
		return result - 1;
	}

	/* recupera todas las llamdas a procedimiento PL/SQL del excel */
	public static ArrayList<String> recuperarLlamadasPL(XSSFSheet sheet) {
		/* todas las lineas de las llamadas a los PL */
		ArrayList<String> l = new ArrayList<String>();
		
		//l.ensureCapacity(10000);

		/* buscamos en el excel la columna donde esta cada llamada al PL */
		int col = buscarColumnaPL(sheet);

		/*
		 * Desde la fila inicial donde comienzan los datos de las SDIS
		 * recuperamos las llamadas a los PL
		 */
		
		try
		{
		int fila = FILAINICIAL;
		boolean cont = true;
		String pl=null;
		while (cont) {
			
			String taux=null;
			try{
				
				XSSFRow filaExcel = sheet.getRow(fila);
				
				if (filaExcel!=null)
				{
					XSSFCell columnaExcel = filaExcel.getCell(col);
					if (columnaExcel!=null)
					{
						taux=columnaExcel.getStringCellValue();
						
						 pl = tratarTextoPL(taux);
						 //System.out.println(fila+"\n");
						if (pl.length() == 0)
							cont = false; // si no tiene contrapartida ya no hay mas SDIs
						else {
							l.add(pl);
							fila++;
						}
					}
					else cont=false;
					
				}
				else cont=false;
				
				
			//taux=sheet.getRow(fila).getCell(col).getStringCellValue();
			}catch(Exception e)
			{
				System.out.println("Error en excel: "+ e.getMessage());
			}
			
			
			
			
		}
		
		}catch(Exception e)
		{
			
			System.out.println(e.getMessage());
		}

		return l;

	}

	/* Trata los argumentos de entrada para obtener los distintos parametros */
	private static void argumentos(String v[])

	{
		int tam = v.length;

		// Si hay algun parametro
		if (tam != 0) {
			rutaFichero = v[0];
			System.out.println("El param es: " + rutaFichero);

			// si hay mas de 1 parametro
			if (tam > 1) {
				try {

					FILAINICIAL = Integer.parseInt(v[1]);
					FILAENCABEZADOS = FILAINICIAL - 1;

				} catch (NumberFormatException e) {
					System.out.println(e.getMessage());

				}
				if (tam > 2) {
					TEXTOCOLUMNAPL = v[2];

				}

			}
		}

	}



	private static void generarReporte(Connection c) throws SQLException {
		System.out.println("USER: " + USER);
		System.out.println("cod: " + cod);

		String SEP="\";\"";
		Statement stmnt2 = c.createStatement();
		String query="SELECT rlt_field AS linea_carga,  CASE    WHEN Rlt_Purp_Typ='ERRORES'    THEN 'N/A'    ELSE trim(SUBSTR(Message_Rlt,(Instrb(Message_Rlt,':',1)+1),Instrb(Message_Rlt,';',1)-Instrb(Message_Rlt,':',1)-1))  END AS RDR_ID,  CASE    WHEN Rlt_Purp_Typ='ERRORES'    THEN Message_Rlt    ELSE 'OK'  END            AS resultado_carga,  Main_Entity_Id AS contrapartida FROM ft_t_rlt1 rlt1 WHERE Rlt_Purp_Typ IN ('REPORTES','ERRORES' ) and JOB_ID='" + USER + cod + "'"+ " ORDER BY Rlt_Purp_Typ desc , To_Number(linea_carga)";
		ResultSet r2 = stmnt2.executeQuery(query);

		
		
		// r2.next();
		// System.out.println(r2.getString(1));

		//linea carga, RDR_ID, resultado_carga (mens), Contrapartida
		
		ArrayList<String> l= new ArrayList<String>();
		
		while (r2.next()) {
			String men=r2.getString(1)+SEP+r2.getString(2)+SEP+r2.getString(3)+SEP+r2.getString(4);
			//System.out.println(men);
		
		//	men="LINEA CARGA: "+r2.getString(2)+" |"+men+" |Entidad Principal: "+r2.getString(3);
			
			//System.out.println("MEN: "+men);
			
			String llamPl="PCK_GESTIONALERTAS.ADD_GESTIONALERTAS_MSG('EXCELROW', 'CARGA_SDI', '"+men+"',  '\";\"')";
			
			l.add(llamPl);
			//Statement stmnt3 = c.createStatement();
			
			//stmnt3.execute("INSERT INTO ft_t_alg1 (alg1_oid,proceso,ald1_oid,tipo,procesado,mensaje,data_stat_typ,last_chg_tms,start_tms,last_chg_usr_id) values (new_oid,'CARGA_SDI','=01342DD7E','MENSAJE','N','"+men+"','ACTIVE',sysdate,sysdate,'AlertasCocinado.jar')");
			
			//stmnt3.close();
		}

		stmnt2.close();
		
		System.out.println("pl a ins del reporte: "+l.size());
		
		realizarLlamadasPL(l, c);
		
		/*cambiar la query y hacer llamadas a PL en lugar de insert*/
		
	}

	private static void realizarLlamadasPL(ArrayList<String> l, Connection c) throws SQLException {
		CallableStatement cs = null;

		int SIZE = l.size();

		for (int i = 0; i < SIZE; i++) {
			String llamada = l.get(i);
			cs = c.prepareCall("{call " + llamada + "}");

			cs.execute();
			cs.close();

		}
	}

	private static void esExcel(String rutaArchivo) {
		Tika tika = new Tika();
		String fileType = null;
		fileType = tika.detect(rutaArchivo);

		System.out.println(fileType);

		if (fileType.contains(MS_EXCEL)) {
			System.out.println("OK, tipo de fichero ");

		} else {
			System.out.println("Tipo de fichero incorrecto");
			System.exit(-1);
		}
	}
}
