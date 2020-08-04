
import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.tika.Tika;

public class RDR_CROSS_Excel_Prueba {
	
	
	
	 public static int getExcelColumnNumber(String column) {
	        int result = 0;
	        for (int i = 0; i < column.length(); i++) {
	            result *= 26;
	            result += column.charAt(i) - 'A' + 1;
	        }
	        return result;
	    }

	public static void main(String[] args) {
		String nombreArchivo = "Plantilla_CargaSDIs.xlsm";//"Inventario.xlsx";
		String rutaArchivo = "C:\\tmp\\os\\" + nombreArchivo;
		String hoja = "Hoja1";
		
		System.out.println("INI");

		try  {
			FileInputStream file = new FileInputStream(new File(rutaArchivo));
			
			
			 Tika tika = new Tika();
	            String fileType = null;
	            fileType = tika.detect(new File(rutaArchivo));

	          
	            	
	            	System.out.println("Es app: "+fileType);
	         
			
			// leer archivo excel
			XSSFWorkbook worbook = new XSSFWorkbook(file);
			// obtener la hoja que se va leer
			XSSFSheet sheet = worbook.getSheetAt(0);
			
			
		//	System.out.println("Directamente: "+sheet.getRow(12).getCell(57).getStringCellValue());
			
			
			// obtener todas las filas de la hoja excel
//			Iterator<Row> rowIterator = sheet.iterator();
//
//			Row row;
//			// se recorre la Tabla
//			while (rowIterator.hasNext()) {
//				row = rowIterator.next();
//				// se obtiene las celdas por fila
//				Iterator<Cell> cellIterator = row.cellIterator();
//				Cell cell;
//				// se recorre cada fila (sus celdas)
//				int j=0;
//				while (cellIterator.hasNext()) {
//					// se obtiene la celda en específico y se la imprime
//					cell = cellIterator.next();
//					System.out.print(j+":"+cell.getStringCellValue() + " | ");
//					j++;
//				}
//				System.out.println();
//			}
			
			//System.out.println("Directamente: "+sheet.getRow(1).getCell(2).getStringCellValue());
			
			
		
			//System.out.println("Directamente: "+sheet.getRow(9).getCell(getExcelColumnNumber("XFD")).getStringCellValue());
			
			System.out.println("Direc******tamente: "+sheet.getRow(8).getCell(1).getStringCellValue());
			
			System.out.println("Directamente: "+sheet.getRow(10).getCell(getExcelColumnNumber("XFD")).getStringCellValue());
			
			
			System.out.println("\n\n***"+getExcelColumnNumber("XFD"));	
			
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
			
		}
		
		System.out.println("FIN");
	}
}
