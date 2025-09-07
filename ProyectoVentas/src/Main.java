import java.io.*;
import java.util.*;

public class Main {

    public static void main(String[] args) {
        try {
            System.out.println("=== PROCESANDO ARCHIVOS ===");
            
            // 1. Reporte de vendedores
            Map<String, Double> ventasVendedores = new HashMap<>();
            
            BufferedReader vendedoresReader = new BufferedReader(new FileReader("vendedores.txt"));
            String linea;
            
            while ((linea = vendedoresReader.readLine()) != null) {
                String[] datos = linea.split(";");
                String nombre = datos[2] + " " + datos[3];
                long id = Long.parseLong(datos[1]);
                
                double totalVentas = calcularVentasVendedor(id);
                ventasVendedores.put(nombre, totalVentas);
            }
            vendedoresReader.close();
            
            generarReporteVendedores(ventasVendedores);
            
            // 2. Reporte de productos
            Map<String, Integer> productosVendidos = new HashMap<>();
            
            vendedoresReader = new BufferedReader(new FileReader("vendedores.txt"));
            while ((linea = vendedoresReader.readLine()) != null) {
                String[] datos = linea.split(";");
                long id = Long.parseLong(datos[1]);
                procesarVentasProductos(id, productosVendidos);
            }
            vendedoresReader.close();
            
            generarReporteProductos(productosVendidos);
            
            System.out.println("=== REPORTES GENERADOS EXITOSAMENTE ===");
            System.out.println("1. reporte_vendedores.txt");
            System.out.println("2. reporte_productos.txt");
            
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static double calcularVentasVendedor(long idVendedor) throws IOException {
        double total = 0;
        try {
            BufferedReader ventasReader = new BufferedReader(new FileReader("ventas_" + idVendedor + ".txt"));
            ventasReader.readLine(); // Saltar primera línea
            
            String venta;
            while ((venta = ventasReader.readLine()) != null) {
                String[] datos = venta.split(";");
                int idProducto = Integer.parseInt(datos[0]);
                int cantidad = Integer.parseInt(datos[1]);
                double precio = obtenerPrecioProducto(idProducto);
                total += cantidad * precio;
            }
            ventasReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("No se encontraron ventas para el vendedor: " + idVendedor);
        }
        return total;
    }

    private static double obtenerPrecioProducto(int idProducto) throws IOException {
        BufferedReader productosReader = new BufferedReader(new FileReader("productos.txt"));
        String linea;
        
        while ((linea = productosReader.readLine()) != null) {
            String[] datos = linea.split(";");
            int id = Integer.parseInt(datos[0]);
            if (id == idProducto) {
                productosReader.close();
                return Double.parseDouble(datos[2]);
            }
        }
        productosReader.close();
        return 0;
    }

    private static void generarReporteVendedores(Map<String, Double> ventas) throws IOException {
        List<Map.Entry<String, Double>> lista = new ArrayList<>(ventas.entrySet());
        lista.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        
        FileWriter writer = new FileWriter("reporte_vendedores.txt");
        for (Map.Entry<String, Double> entry : lista) {
        	writer.write(entry.getKey() + ";" + String.format("%.0f", entry.getValue()) + "\n");
        }
        writer.close();
        System.out.println("Reporte de vendedores generado");
    }

    private static void procesarVentasProductos(long idVendedor, Map<String, Integer> productos) throws IOException {
        try {
            BufferedReader ventasReader = new BufferedReader(new FileReader("ventas_" + idVendedor + ".txt"));
            ventasReader.readLine(); // Saltar primera línea
            
            String venta;
            while ((venta = ventasReader.readLine()) != null) {
                String[] datos = venta.split(";");
                int idProducto = Integer.parseInt(datos[0]);
                int cantidad = Integer.parseInt(datos[1]);
                
                String nombreProducto = obtenerNombreProducto(idProducto);
                productos.put(nombreProducto, productos.getOrDefault(nombreProducto, 0) + cantidad);
            }
            ventasReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("No se encontraron ventas para el vendedor: " + idVendedor);
        }
    }

    private static String obtenerNombreProducto(int idProducto) throws IOException {
        BufferedReader productosReader = new BufferedReader(new FileReader("productos.txt"));
        String linea;
        
        while ((linea = productosReader.readLine()) != null) {
            String[] datos = linea.split(";");
            int id = Integer.parseInt(datos[0]);
            if (id == idProducto) {
                productosReader.close();
                return datos[1];
            }
        }
        productosReader.close();
        return "Desconocido";
    }

    private static void generarReporteProductos(Map<String, Integer> productos) throws IOException {
        List<Map.Entry<String, Integer>> lista = new ArrayList<>(productos.entrySet());
        lista.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        
        FileWriter writer = new FileWriter("reporte_productos.txt");
        for (Map.Entry<String, Integer> entry : lista) {
            writer.write(entry.getKey() + ";" + entry.getValue() + "\n");
        }
        writer.close();
        System.out.println("Reporte de productos generado");
    }
}
