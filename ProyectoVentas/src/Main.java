import java.io.*;
import java.util.*;

public class Main {

    public static void main(String[] args) {
        System.out.println("=== INICIANDO PROCESAMIENTO DE ARCHIVOS ===");
        System.out.println("Elementos Extra Implementados:");
        System.out.println("a. Procesar múltiples archivos por vendedor");
        System.out.println("b. Trabajar con archivos serializados");
        System.out.println("c. Detección de errores y datos incoherentes");
        
        try {
              if (new File("productos_serializados.dat").exists()) {
                System.out.println("Procesando archivo serializado...");
                procesarArchivoSerializado();
            }
            
         
            Map<String, Double> ventasPorVendedor = procesarVentasVendedores();
            generarReporteVendedores(ventasPorVendedor);
            
             Map<String, Integer> productosVendidos = procesarProductosVendidos();
            generarReporteProductos(productosVendidos);
            
            System.out.println("=== PROCESAMIENTO COMPLETADO EXITOSAMENTE ===");
            System.out.println("Reportes generados:");
            System.out.println("- reporte_vendedores.csv");
            System.out.println("- reporte_productos.csv");
            
        } catch (IOException e) {
            System.err.println("Error durante el procesamiento: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private static void procesarArchivoSerializado() throws IOException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("productos_serializados.dat"))) {
            @SuppressWarnings("unchecked")
            List<GenerateInfoFiles.Producto> productos = (List<GenerateInfoFiles.Producto>) ois.readObject();
            
            System.out.println("Productos cargados desde archivo serializado:");
            for (GenerateInfoFiles.Producto producto : productos) {
                System.out.println("  - " + producto.getNombre() + ": $" + producto.getPrecio());
            }
            
        } catch (ClassNotFoundException e) {
            System.err.println("Error al leer archivo serializado: " + e.getMessage());
        }
    }

    private static Map<String, Double> procesarVentasVendedores() throws IOException {
        Map<String, Double> ventasVendedores = new HashMap<>();
        
        System.out.println("Procesando información de vendedores...");
        
        try (BufferedReader vendedoresReader = new BufferedReader(new FileReader("vendedores.txt"))) {
            String linea;
            
            while ((linea = vendedoresReader.readLine()) != null) {
                if (!validarFormatoLinea(linea, 4, "vendedores.txt")) {
                    continue; 
                }
                
                String[] datosVendedor = linea.split(";");
                String nombreCompleto = datosVendedor[2] + " " + datosVendedor[3];
                long numeroDocumento = Long.parseLong(datosVendedor[1]);
                
                double totalVentas = calcularVentasVendedor(numeroDocumento);
                ventasVendedores.put(nombreCompleto, totalVentas);
                
                System.out.println("Vendedor procesado: " + nombreCompleto + " - Ventas: $" + totalVentas);
            }
        }
        
        return ventasVendedores;
    }

  static double calcularVentasVendedor(long idVendedor) throws IOException {
        double totalVentas = 0.0;
        
  
        for (int archivoNum = 1; archivoNum <= 10; archivoNum++) { 
            String archivoVentas = "ventas_" + idVendedor + "_" + archivoNum + ".txt";
            File file = new File(archivoVentas);
            
            if (!file.exists()) {
                
                if (archivoNum == 1) {
                    archivoVentas = "ventas_" + idVendedor + ".txt";
                    file = new File(archivoVentas);
                    if (!file.exists()) {
                        break; 
                    }
                } else {
                    break; 
                }
            }
            
            System.out.println("  Procesando archivo: " + archivoVentas);
            totalVentas += procesarArchivoVentas(archivoVentas);
        }
        
        return totalVentas;
    }

    private static double procesarArchivoVentas(String archivoVentas) throws IOException {
        double totalArchivo = 0.0;
        int lineasProcesadas = 0;
        int lineasConError = 0;
        
        try (BufferedReader ventasReader = new BufferedReader(new FileReader(archivoVentas))) {
            String encabezado = ventasReader.readLine();
            if (encabezado == null || !encabezado.contains(";")) {
                System.err.println("  Encabezado inválido en: " + archivoVentas);
                return 0.0;
            }
            
            String lineaVenta;
            while ((lineaVenta = ventasReader.readLine()) != null) {
                lineasProcesadas++;
                
                if (!validarFormatoLinea(lineaVenta, 2, archivoVentas)) {
                    lineasConError++;
                    continue;
                }
                
                String[] datosVenta = lineaVenta.split(";");
                try {
                    int idProducto = Integer.parseInt(datosVenta[0]);
                    int cantidadVendida = Integer.parseInt(datosVenta[1].replace(";", ""));
                    
                    if (cantidadVendida <= 0) {
                        System.err.println("  Cantidad inválida en " + archivoVentas + ": " + cantidadVendida);
                        lineasConError++;
                        continue;
                    }
                    
                    double precioProducto = obtenerPrecioProducto(idProducto);
                    if (precioProducto <= 0) {
                        System.err.println("  Precio inválido para producto ID: " + idProducto);
                        lineasConError++;
                        continue;
                    }
                    
                    totalArchivo += cantidadVendida * precioProducto;
                    
                } catch (NumberFormatException e) {
                    System.err.println("  Error de formato numérico en " + archivoVentas + ": " + lineaVenta);
                    lineasConError++;
                }
            }
            
            if (lineasConError > 0) {
                System.err.println("  Archivo " + archivoVentas + ": " + lineasConError + "/" + lineasProcesadas + " líneas con errores");
            }
            
        } catch (FileNotFoundException e) {
            System.err.println("  Archivo no encontrado: " + archivoVentas);
        }
        
        return totalArchivo;
    }

    private static boolean validarFormatoLinea(String linea, int camposEsperados, String nombreArchivo) {
        if (linea == null || linea.trim().isEmpty()) {
            return false;
        }
        
        String[] campos = linea.split(";");
        if (campos.length < camposEsperados) {
            System.err.println("  Formato inválido en " + nombreArchivo + ": " + linea);
            return false;
        }
        
        return true;
    }

    private static Map<String, Integer> procesarProductosVendidos() throws IOException {
        Map<String, Integer> productosVendidos = new HashMap<>();
        
        System.out.println("Procesando productos vendidos...");
        
        try (BufferedReader vendedoresReader = new BufferedReader(new FileReader("vendedores.txt"))) {
            String linea;
            
            while ((linea = vendedoresReader.readLine()) != null) {
                if (!validarFormatoLinea(linea, 4, "vendedores.txt")) {
                    continue;
                }
                
                String[] datosVendedor = linea.split(";");
                long numeroDocumento = Long.parseLong(datosVendedor[1]);
                procesarVentasProductos(numeroDocumento, productosVendidos);
            }
        }
        
        return productosVendidos;
    }

    private static void procesarVentasProductos(long idVendedor, Map<String, Integer> productosVendidos) 
            throws IOException {
        // Procesar múltiples archivos de ventas (Elemento Extra a)
        for (int archivoNum = 1; archivoNum <= 10; archivoNum++) {
            String archivoVentas = "ventas_" + idVendedor + "_" + archivoNum + ".txt";
            File file = new File(archivoVentas);
            
            if (!file.exists()) {
                if (archivoNum == 1) {
                    archivoVentas = "ventas_" + idVendedor + ".txt";
                    file = new File(archivoVentas);
                    if (!file.exists()) {
                        break;
                    }
                } else {
                    break;
                }
            }
            
            procesarUnArchivoProductos(archivoVentas, productosVendidos);
        }
    }

    private static void procesarUnArchivoProductos(String archivoVentas, Map<String, Integer> productosVendidos) 
            throws IOException {
        try (BufferedReader ventasReader = new BufferedReader(new FileReader(archivoVentas))) {
            ventasReader.readLine(); 
            
            String lineaVenta;
            while ((lineaVenta = ventasReader.readLine()) != null) {
                if (!validarFormatoLinea(lineaVenta, 2, archivoVentas)) {
                    continue;
                }
                
                String[] datosVenta = lineaVenta.split(";");
                try {
                    int idProducto = Integer.parseInt(datosVenta[0]);
                    int cantidadVendida = Integer.parseInt(datosVenta[1].replace(";", ""));
                    
                    if (cantidadVendida > 0) {
                        String nombreProducto = obtenerNombreProducto(idProducto);
                        if (!"Producto Desconocido".equals(nombreProducto)) {
                            productosVendidos.put(nombreProducto, 
                                productosVendidos.getOrDefault(nombreProducto, 0) + cantidadVendida);
                        }
                    }
                    
                } catch (NumberFormatException e) {
                    System.err.println("  Error numérico en " + archivoVentas + ": " + lineaVenta);
                }
            }
            
        } catch (FileNotFoundException e) {
            System.err.println("  Archivo no encontrado: " + archivoVentas);
        }
    }

    private static double obtenerPrecioProducto(int idProducto) throws IOException {
        try (BufferedReader productosReader = new BufferedReader(new FileReader("productos.txt"))) {
            String linea;
            
            while ((linea = productosReader.readLine()) != null) {
                if (!validarFormatoLinea(linea, 3, "productos.txt")) {
                    continue;
                }
                
                String[] datosProducto = linea.split(";");
                int id = Integer.parseInt(datosProducto[0]);
                
                if (id == idProducto) {
                    double precio = Double.parseDouble(datosProducto[2]);
                    return precio > 0 ? precio : 0;
                }
            }
        }
        
        return 0.0;
    }

    private static String obtenerNombreProducto(int idProducto) throws IOException {
        try (BufferedReader productosReader = new BufferedReader(new FileReader("productos.txt"))) {
            String linea;
            
            while ((linea = productosReader.readLine()) != null) {
                if (!validarFormatoLinea(linea, 2, "productos.txt")) {
                    continue;
                }
                
                String[] datosProducto = linea.split(";");
                int id = Integer.parseInt(datosProducto[0]);
                
                if (id == idProducto) {
                    return datosProducto[1];
                }
            }
        }
        
        return "Producto Desconocido";
    }


    private static void generarReporteVendedores(Map<String, Double> ventasVendedores) throws IOException {
        List<Map.Entry<String, Double>> listaVendedores = new ArrayList<>(ventasVendedores.entrySet());
        listaVendedores.sort((v1, v2) -> v2.getValue().compareTo(v1.getValue()));
        
        try (FileWriter writer = new FileWriter("reporte_vendedores.csv")) {
            writer.write("Vendedor;Total_Ventas\n");
            for (Map.Entry<String, Double> vendedor : listaVendedores) {
                String linea = vendedor.getKey() + ";" + String.format("%.0f", vendedor.getValue());
                writer.write(linea + "\n");
            }
        }
        
        System.out.println("Reporte de vendedores generado: reporte_vendedores.csv");
    }

    private static void generarReporteProductos(Map<String, Integer> productosVendidos) throws IOException {
        List<Map.Entry<String, Integer>> listaProductos = new ArrayList<>(productosVendidos.entrySet());
        listaProductos.sort((p1, p2) -> p2.getValue().compareTo(p1.getValue()));
        
        try (FileWriter writer = new FileWriter("reporte_productos.csv")) {
            writer.write("Producto;Precio_Unitario;Cantidad_Vendida\n");
            for (Map.Entry<String, Integer> producto : listaProductos) {
                String nombreProducto = producto.getKey();
                double precio = obtenerPrecioProductoPorNombre(nombreProducto);
                int cantidad = producto.getValue();
                
                String linea = nombreProducto + ";" + String.format("%.0f", precio) + ";" + cantidad;
                writer.write(linea + "\n");
            }
        }
        
        System.out.println("Reporte de productos generado: reporte_productos.csv");
    }

    private static double obtenerPrecioProductoPorNombre(String nombreProducto) throws IOException {
        try (BufferedReader productosReader = new BufferedReader(new FileReader("productos.txt"))) {
            String linea;
            
            while ((linea = productosReader.readLine()) != null) {
                if (!validarFormatoLinea(linea, 3, "productos.txt")) {
                    continue;
                }
                
                String[] datosProducto = linea.split(";");
                if (datosProducto[1].equals(nombreProducto)) {
                    return Double.parseDouble(datosProducto[2]);
                }
            }
        }
        
        return 0.0;
    }

}