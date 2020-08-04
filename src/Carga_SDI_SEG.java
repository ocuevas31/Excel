
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.sl.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//import org.apache.tika.Tika;

/*La fila 10 en excel (la 9 en codigo) tiene el nombre de las columnas y tiene que tener una llamada PL que es donde se encontrara la llamada a la funcion VB loadSDI(...*/
/*Si el nombre de la Cparty es vacio -> no carga mas SDIs*/
/*La ruta de la que obtener el fichero es el primer parametro
 * El numero MAXIMO de SDIS a cargar es el segundo params*/

/*PARAMETROS   
 * 
 *  numero maximo de SDIs a cargar
 * */

public class Carga_SDI_SEG {

	private final static int FILAINICIAL = 9;
	private static final int COLUMNAINICIAL = 57;
	private static final int FILAENCABEZADOS = 8;
	public static final String TEXTOCOLUMNAPL = "PL";
	public static int MAXFILAS = 5000;
	public static String rutaFichero = null;
	public static final String VACIA = "";
	static final String[] parametros = { "Ruta fich", "Maxfiles" };
	static String USER;

	/*
	 * 
	 * 
	 * 
	 * crear constructor y pillar los argumentos
	 * 
	 * 
	 */

	static private long cod;

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

	private static String buscarArchivo(File ruta, String extension) {
		// Creo el vector que contendra todos los archivos de una ruta
		// especificada.
		File[] archivo = ruta.listFiles();
		// Evaluo si la carpeta especificada contiene archivos.
		if (archivo != null) {
			// Recorro el vector el cual tiene almacenado la ruta del archivo a
			// buscar.
			for (int i = 0; i < archivo.length; i++) {
				File Arc = archivo[i];
				// Evaluo si el archivo o la ruta es una carpeta.
				if (!archivo[i].isDirectory()) {
					//
					// Evaluo el tipo de extencion.
					if (archivo[i].getName().endsWith(extension)) {

						return (archivo[i].getName());
					}

				}

			}
		}
		return null;
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

			nuevoPl = pl.substring(0, posLastComa) + user + cod + "')";

		} catch (Exception e) {
			nuevoPl = VACIA;

		}
		return nuevoPl;

	}

	public static int getExcelColumnNumber(String column) {
		int result = 0;
		for (int i = 0; i < column.length(); i++) {
			result *= 26;
			result += column.charAt(i) - 'A' + 1;
		}
		return result - 1;
	}

	public static ArrayList<String> recuperarLlamadasPL(XSSFSheet sheet) {
		/* todas las lineas de las llamadas a los PL */
		ArrayList<String> l = new ArrayList<String>();

		/* buscamos en el excel la columna donde esta cada llamada al PL */
		int col = buscarColumnaPL(sheet);

		/*
		 * Desde la fila inicial donde comienzan los datos de las SDIS
		 * recuperamos las llamadas a los PL
		 */
		for (int fila = FILAINICIAL; fila < MAXFILAS; fila++) {
			String pl = tratarTextoPL(sheet.getRow(fila).getCell(col).getStringCellValue());
			if (pl.length() == 0)
				return l;
			l.add(pl);
		}

		return l;

	}

	/**
	 * Devuelve el tipo del fichero
	 * 
	 * @throws IOException
	 */
	// private static String getFileType(File f) throws IOException {
	//
	// FileInputStream file = new FileInputStream(f);
	//
	// Tika tika = new Tika();
	// return tika.detect(f);
	//
	// }
	//

	/* Numero de filas de verdad NOOOOOOOOOOOOOOOOOOOO */

	public static int getLastRowWithData(XSSFSheet s) {
		int rowCount = 0;
		Iterator<Row> iter = s.rowIterator();

		while (iter.hasNext()) {
			Row r = iter.next();
			if (isRowBlank(r)) {
				rowCount = r.getRowNum();
			}
		}

		return rowCount;
	}

	public static boolean isRowBlank(Row r) {
		boolean ret = true;

		/*
		 * If a row is null, it must be blank.
		 */
		if (r != null) {
			Iterator<Cell> cellIter = r.cellIterator();
			/*
			 * Iterate through all cells in a row.
			 */
			while (cellIter.hasNext()) {
				/*
				 * If one of the cells in given row contains data, the row is
				 * considered not blank.
				 */
				if (isCellBlank(cellIter.next())) {
					ret = false;
					break;
				}
			}
		}

		return ret;
	}

	public static boolean isCellBlank(Cell c) {
		return (c == null || c.getCellType() == Cell.CELL_TYPE_BLANK);
	}

	public boolean isCellEmpty(Cell c) {
		return (c == null || c.getCellType() == Cell.CELL_TYPE_BLANK
				|| (c.getCellType() == Cell.CELL_TYPE_STRING && c.getStringCellValue().isEmpty()));
	}

	/* numero de filas de verdad NOOOOOOOOOOOOOOOO */

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

					MAXFILAS = Integer.parseInt(v[1]);

				} catch (NumberFormatException e) {
					System.out.println(e.getMessage());

				}

			}

		}

	}

	public static void main(String[] args) {

		System.out.println("INI");

		
		
		
		System.out.println(buscarArchivo(new File("/tmp/os/"), ".xlsm"));
		System.exit(1);
		

		argumentos(args);

		String nombreArchivo = getFileName();
		String ruta = getPath();
		String rutaArchivo = ruta + nombreArchivo;
		;

		System.out.println("ruta Fich: " + rutaArchivo);

		System.out.println("maxfilas: " + MAXFILAS);

		try {

			FileInputStream file = new FileInputStream(new File(rutaArchivo));

			// System.out.println("Es app: " + getFileType(new
			// File(rutaArchivo)));

			// leer archivo excel
			XSSFWorkbook worbook = new XSSFWorkbook(file);
			// obtener la hoja que se va leer
			XSSFSheet sheet = worbook.getSheetAt(0);

			// System.out.println("REs buscar: "+buscarColumnaPL(sheet));
			// Iterator<Row> rowIterator = sheet.iterator();

			// System.out.println("FILAS: " + getLastRowWithData(sheet));
			// System.out.println("FILAS MAX: " + sheet.getLastRowNum());

			// int filaInicial = FILAINICIAL;
			// int columnaInicial = COLUMNAINICIAL;
			//
			// int columna = columnaInicial;
			// for (int fila = filaInicial; fila < 100; fila++) {
			// System.out.println(sheet.getRow(fila).getCell(columna).getStringCellValue());
			// }

			// obtener todas las filas de la hoja excel Iterator<Row>
			// rowIterator = sheet.iterator();

			// Row row; // se recorre la Tabla
			// int j=0;
			// while (rowIterator.hasNext()) {

			// row = rowIterator.next(); // se obtiene las celdas por fila
			// Iterator<Cell> cellIterator = row.cellIterator();
			// Cell cell; // se recorre cada fila (sus celdas) int j=0;
			// while (cellIterator.hasNext()) {
			// se obtiene la celda en específico y se la imprime cell =
			// cellIterator.next();
			// System.out.print(j + ":" + cell.getStringCellValue() + " | ");

			// }
			// j++;
			// System.out.println();
			// }

			// System.out.println("Directamente: "
			// +sheet.getRow(1).getCell(2).getStringCellValue());

			// System.out.println("FILAS RECORRIDAS: "+j);

			// System.out.println("Directamente:
			// "+sheet.getRow(9).getCell(54).getNumericCellValue());

			// //cambiar 2018xxxxx por SystemTimeCurree...
			// String clave="";
			// String formula="loadSDI(F10:BE10"+clave+")";
			//
			// int f=1;
			// int c=4;
			// Cell x=sheet.getRow(f).createCell(c);
			//
			// // x.setCellType(HSSFCell.CELL_TYPE_FORMULA);
			//
			//
			// x.setCellFormula(formula);
			//
			//
			//

			// System.out.println("Directamente:"+sheet.getRow(FILAINICIAL).getCell(COLUMNAINICIAL).getStringCellValue());

			// System.out.println("Directamente:"+sheet.getRow(1).getCell(1).getStringCellValue());

			// FormulaEvaluator formulaEvaluator =
			// worbook.getCreationHelper().createFormulaEvaluator();
			// formulaEvaluator.evaluate(sheet.getRow(f).getCell(c));

			// System.out.println("Directamente:"+sheet.getRow(6).getCell(0).getStringCellValue());

			/*
			 * Hay que almacenar el codigo de tiempo al ppio para ponerles a
			 * todos los pl el mismo
			 */
			cod = System.currentTimeMillis();

			// String
			// pl=sheet.getRow(9).getCell(buscarColumnaPL(sheet)).getStringCellValue();
			// System.out.println("Dir***ectamente:"+pl);

			// pl=tratarTextoPL(pl);
			// System.out.println("re2"+pl);

			ArrayList<String> l = recuperarLlamadasPL(sheet);

			System.out.println(l);

			////////////// BBDDD

			DDBB db = new DDBB();

			db.ObtenerCredenciales();

			Connection c = db.ObtenerConexion();

			try {
				CallableStatement cs = null;

				String llamada = l.get(0);
				cs = c.prepareCall("{call " + llamada + "}");

				cs.execute();

				cs.close();

				/****/

				System.out.println("USER: " + USER);
				System.out.println("cod: " + cod);

				Statement stmnt2 = c.createStatement();
				ResultSet r2 = stmnt2
						.executeQuery("select message_rlt from ft_T_rlt1 where LAST_CHG_USR_ID='" + USER + cod + "'");

				// r2.next();
				// System.out.println(r2.getString(1));

				while (r2.next()) {
					System.out.println(r2.getString(1));

				}

				stmnt2.close();

				/****/

				c.close();

			} catch (Exception e) {

				System.out.println(e.getMessage());
			}

		} catch (Exception e) {
			System.out.println(e.getMessage());

		}

		System.out.println("FIN");
	}
}
