import java.io.*;
import java.util.*;

public class Main {

    // Punto de partida de nuestro sistema de procesamiento
    // Aquí es donde comenzamos a analizar todos los datos de ventas
    public static void main(String[] args) {
        System.out.println("=== INICIANDO PROCESAMIENTO DE ARCHIVOS ===");
        System.out.println("Elementos Extra Implementados:");
        System.out.println("a. Procesar múltiples archivos por vendedor");
        System.out.println("b. Trabajar con archivos serializados");
        System.out.println("c. Detección de errores y datos incoherentes");
        
        try {
            // Primero revisamos si existe la versión especial de productos guardada
            if (new File("productos_serializados.dat").exists()) {
                System.out.println("Procesando archivo serializado...");
                procesarArchivoSerializado();
            }
            
            // Procesamos las ventas de todos nuestros vendedores
            Map<String, Double> ventasPorVendedor = procesarVentasVendedores();
            generarReporteVendedores(ventasPorVendedor);
            
            // También analizamos qué productos se vendieron más
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

    // Lee la versión especial de productos que guardamos anteriormente
    // Es como abrir una caja fuerte con información importante
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

    // Analiza cuánto vendió cada vendedor en total
    // Revisa todos los vendedores y suma sus ventas
    private static Map<String, Double> procesarVentasVendedores() throws IOException {
        Map<String, Double> ventasVendedores = new HashMap<>();
        
        System.out.println("Procesando información de vendedores...");
        
        // Leemos el archivo maestro de vendedores
        try (BufferedReader vendedoresReader = new BufferedReader(new FileReader("vendedores.txt"))) {
            String linea;
            
            // Procesamos cada vendedor uno por uno
            while ((linea = vendedoresReader.readLine()) != null) {
                // Verificamos que la información del vendedor esté completa
                if (!validarFormatoLinea(linea, 4, "vendedores.txt")) {
                    continue; // Si está incompleta, pasamos al siguiente
                }
                
                // Extraemos los datos del vendedor
                String[] datosVendedor = linea.split(";");
                String nombreCompleto = datosVendedor[2] + " " + datosVendedor[3];
                long numeroDocumento = Long.parseLong(datosVendedor[1]);
                
                // Calculamos cuánto vendió este vendedor en total
                double totalVentas = calcularVentasVendedor(numeroDocumento);
                ventasVendedores.put(nombreCompleto, totalVentas);
                
                System.out.println("Vendedor procesado: " + nombreCompleto + " - Ventas: $" + totalVentas);
            }
        }
        
        return ventasVendedores;
    }

    // Calcula el total de ventas para un vendedor específico
    // Revisa todos sus archivos de ventas y suma todo
    static double calcularVentasVendedor(long idVendedor) throws IOException {
        double totalVentas = 0.0;
        
        // Un vendedor puede tener varios archivos de ventas (ventas_1, ventas_2, etc.)
        for (int archivoNum = 1; archivoNum <= 10; archivoNum++) { 
            String archivoVentas = "ventas_" + idVendedor + "_" + archivoNum + ".txt";
            File file = new File(archivoVentas);
            
            // Si no existe el archivo numerado, probamos con el archivo simple
            if (!file.exists()) {
                if (archivoNum == 1) {
                    archivoVentas = "ventas_" + idVendedor + ".txt";
                    file = new File(archivoVentas);
                    if (!file.exists()) {
                        break; // Si tampoco existe, terminamos
                    }
                } else {
                    break; // Si no hay más archivos, terminamos
                }
            }
            
            System.out.println("  Procesando archivo: " + archivoVentas);
            totalVentas += procesarArchivoVentas(archivoVentas);
        }
        
        return totalVentas;
    }

    // Procesa un archivo individual de ventas
    // Lee cada venta y calcula su valor total
    private static double procesarArchivoVentas(String archivoVentas) throws IOException {
        double totalArchivo = 0.0;
        int lineasProcesadas = 0;
        int lineasConError = 0;
        
        try (BufferedReader ventasReader = new BufferedReader(new FileReader(archivoVentas))) {
            // La primera línea es el encabezado con información del vendedor
            String encabezado = ventasReader.readLine();
            if (encabezado == null || !encabezado.contains(";")) {
                System.err.println("  Encabezado inválido en: " + archivoVentas);
                return 0.0;
            }
            
            // Procesamos cada venta línea por línea
            String lineaVenta;
            while ((lineaVenta = ventasReader.readLine()) != null) {
                lineasProcesadas++;
                
                // Verificamos que la venta tenga el formato correcto
                if (!validarFormatoLinea(lineaVenta, 2, archivoVentas)) {
                    lineasConError++;
                    continue; // Si está mal formada, la saltamos
                }
                
                String[] datosVenta = lineaVenta.split(";");
                try {
                    int idProducto = Integer.parseInt(datosVenta[0]);
                    int cantidadVendida = Integer.parseInt(datosVenta[1].replace(";", ""));
                    
                    // Verificamos que la cantidad sea válida
                    if (cantidadVendida <= 0) {
                        System.err.println("  Cantidad inválida en " + archivoVentas + ": " + cantidadVendida);
                        lineasConError++;
                        continue;
                    }
                    
                    // Buscamos el precio del producto vendido
                    double precioProducto = obtenerPrecioProducto(idProducto);
                    if (precioProducto <= 0) {
                        System.err.println("  Precio inválido para producto ID: " + idProducto);
                        lineasConError++;
                        continue;
                    }
                    
                    // Sumamos al total: cantidad × precio
                    totalArchivo += cantidadVendida * precioProducto;
                    
                } catch (NumberFormatException e) {
                    System.err.println("  Error de formato numérico en " + archivoVentas + ": " + lineaVenta);
                    lineasConError++;
                }
            }
            
            // Mostramos un resumen de errores si los hubo
            if (lineasConError > 0) {
                System.err.println("  Archivo " + archivoVentas + ": " + lineasConError + "/" + lineasProcesadas + " líneas con errores");
            }
            
        } catch (FileNotFoundException e) {
            System.err.println("  Archivo no encontrado: " + archivoVentas);
        }
        
        return totalArchivo;
    }

    // Verifica que una línea de datos tenga el formato correcto
    // Como un inspector de calidad para nuestros datos
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

    // Cuenta cuántas unidades se vendieron de cada producto
    // Para saber qué productos son los más populares
    private static Map<String, Integer> procesarProductosVendidos() throws IOException {
        Map<String, Integer> productosVendidos = new HashMap<>();
        
        System.out.println("Procesando productos vendidos...");
        
        // Revisamos las ventas de cada vendedor para contar productos
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

    // Revisa todos los archivos de ventas de un vendedor para contar productos
    private static void procesarVentasProductos(long idVendedor, Map<String, Integer> productosVendidos) 
            throws IOException {
        // Un vendedor puede tener varios archivos de ventas
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

    // Procesa un archivo individual para contar productos vendidos
    private static void procesarUnArchivoProductos(String archivoVentas, Map<String, Integer> productosVendidos) 
            throws IOException {
        try (BufferedReader ventasReader = new BufferedReader(new FileReader(archivoVentas))) {
            ventasReader.readLine(); // Saltamos el encabezado
            
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
                            // Sumamos la cantidad vendida al total del producto
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

    // Busca el precio de un producto usando su ID
    // Como consultar el precio en un catálogo
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

    // Busca el nombre de un producto usando su ID
    // Para saber cómo se llama el producto que se vendió
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

    // Genera el reporte final de vendedores ordenado por mejores ventas
    // Como crear un ranking de los vendedores más exitosos
    private static void generarReporteVendedores(Map<String, Double> ventasVendedores) throws IOException {
        // Ordenamos los vendedores de mayor a menor ventas
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

    // Genera el reporte final de productos más vendidos
    // Para saber qué productos son los favoritos de los clientes
    private static void generarReporteProductos(Map<String, Integer> productosVendidos) throws IOException {
        // Ordenamos los productos de más a menos vendidos
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

    // Busca el precio de un producto usando su nombre
    // Para completar la información en el reporte final
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