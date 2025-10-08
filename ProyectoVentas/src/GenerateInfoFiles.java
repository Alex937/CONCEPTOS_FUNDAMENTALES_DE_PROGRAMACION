import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GenerateInfoFiles {
    
    public static class Producto implements Serializable {
        private static final long serialVersionUID = 1L;
        private int id;
        private String nombre;
        private double precio;
        
        public Producto(int id, String nombre, double precio) {
            this.id = id;
            this.nombre = nombre;
            this.precio = precio;
        }
        
        
        public int getId() { return id; }
        public String getNombre() { return nombre; }
        public double getPrecio() { return precio; }
        
        
        public String toString() {
            return "Producto{id=" + id + ", nombre='" + nombre + "', precio=" + precio + "}";
        }
    }
    
    // Nombres de pila para nuestros vendedores
    private static final String[] NOMBRES = {
        "Andres", "Camila", "Natalia", "Leonardo", "Ivan", "Liliana", "Omar"
    };
    
    // Apellidos para completar los nombres de los vendedores
    private static final String[] APELLIDOS = {
        "Sierra", "Castañeda", "Orozco", "Rojas", "Medina", "Acosta", "Parra"
    };
    
    // Los productos que nuestra tienda tiene disponibles para vender
    private static final String[] PRODUCTOS = {
        "Portatil", "Celular", "Televisor", "Auriculares", "Teclado USB", "Mouse usb", "Router"
    };
    
    // Los precios correspondientes a cada producto
    private static final double[] PRECIOS = {
        2800000, 750000, 1280000, 50000, 40000, 21000, 150000
    };
    
    // Nos ayuda a generar números aleatorios para simular datos reales
    private static final Random random = new Random();

    // Crea el archivo de ventas para un vendedor en particular
    // Cada vendedor tiene su propio archivo con su historial de ventas
    public static void createSalesMenFile(int randomSalesCount, String name, long id) {
        String fileName = "ventas_" + id + ".txt";
        
        try (FileWriter writer = new FileWriter(fileName)) {
            // Primero escribimos la identificación del vendedor
            writer.write("CC;" + id + "\n");
            
            // Luego generamos cada una de las ventas que hizo este vendedor
            for (int i = 0; i < randomSalesCount; i++) {
                int productId = 100 + random.nextInt(7);  // Elegimos un producto al azar
                int cantidad = random.nextInt(10) + 1;    // Cantidad vendida entre 1 y 10
                writer.write(productId + ";" + cantidad + ";\n");
            }
            
            System.out.println("Archivo de ventas creado: " + fileName);
            
        } catch (IOException e) {
            System.err.println("Error al crear archivo de ventas: " + e.getMessage());
        }
    }

    // Crea el archivo maestro de productos con todos los artículos disponibles
    // Este archivo funciona como el catálogo de nuestra tienda
    public static void createProductsFile(int productsCount) {
        String fileName = "productos.txt";
        
        try (FileWriter writer = new FileWriter(fileName)) {
            // Para cada producto, creamos una línea con toda su información
            for (int i = 0; i < productsCount; i++) {
                int productId = 100 + i;
                String productName = PRODUCTOS[i];
                double price = PRECIOS[i];
                
                // Formateamos el precio para que no tenga decimales
                writer.write(productId + ";" + productName + ";" + String.format("%.0f", price) + "\n");
            }
            
            System.out.println("Archivo de productos creado: " + fileName);
            
        } catch (IOException e) {
            System.err.println("Error al crear archivo de productos: " + e.getMessage());
        }
    }

    // Crea el archivo con información de todos nuestros vendedores
    // Además, para cada vendedor crea su propio archivo de ventas
    public static void createSalesManInfoFile(int salesmanCount) {
        String fileName = "vendedores.txt";
        
        try (FileWriter writer = new FileWriter(fileName)) {
            // Creamos la información para cada vendedor del equipo
            for (int i = 0; i < salesmanCount; i++) {
                String tipoDocumento = "CC";
                long numeroDocumento = 1000000000L + i;
                // Combinamos nombres y apellidos al azar
                String nombre = NOMBRES[random.nextInt(NOMBRES.length)];
                String apellido = APELLIDOS[random.nextInt(APELLIDOS.length)];
                
                // Guardamos los datos del vendedor en el archivo maestro
                writer.write(tipoDocumento + ";" + numeroDocumento + ";" + nombre + ";" + apellido + "\n");
                
                // Ahora creamos el archivo personal de ventas para este vendedor
                int numeroVentas = random.nextInt(5) + 1; // Cada vendedor tiene entre 1 y 5 ventas
                createSalesMenFile(numeroVentas, nombre + "_" + apellido, numeroDocumento);
            }
            
            System.out.println("Archivo de vendedores creado: " + fileName);
            
        } catch (IOException e) {
            System.err.println("Error al crear archivo de vendedores: " + e.getMessage());
        }
    }

    // Crea una versión especial de los productos que se puede guardar y cargar fácilmente
    // Este formato es útil para mantener la información entre ejecuciones del programa
    public static void createSerializedProductsFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("productos_serializados.dat"))) {
            List<Producto> productos = new ArrayList<>();
            // Convertimos cada producto en un objeto y lo añadimos a la lista
            for (int i = 0; i < PRODUCTOS.length; i++) {
                productos.add(new Producto(100 + i, PRODUCTOS[i], PRECIOS[i]));
            }
            // Guardamos toda la lista de productos en el archivo
            oos.writeObject(productos);
            System.out.println("Archivo serializado de productos creado: productos_serializados.dat");
        } catch (IOException e) {
            System.err.println("Error al crear archivo serializado: " + e.getMessage());
        }
    }

    // Este es el método principal que pone todo en marcha
    // Aquí es donde empezamos a generar todos los archivos que necesitamos
    public static void main(String[] args) {
        System.out.println("=== INICIANDO GENERACIÓN DE ARCHIVOS DE PRUEBA ===");
        
        try {
            // Lo primero que necesitamos es saber qué productos vendemos
            createProductsFile(7);
            
            // Luego necesitamos conocer a nuestros vendedores y sus ventas
            createSalesManInfoFile(3);
            
            // Finalmente creamos una copia especial de los productos
            createSerializedProductsFile();
            
            System.out.println("=== GENERACIÓN DE ARCHIVOS COMPLETADA EXITOSAMENTE ===");
            System.out.println("Archivos generados:");
            System.out.println("- productos.txt");
            System.out.println("- vendedores.txt");
            System.out.println("- ventas_XXXXXXX.txt (archivos de ventas por vendedor)");
            System.out.println("- productos_serializados.dat (archivo serializado)");
            
        } catch (Exception e) {
            System.err.println("Error durante la generación de archivos: " + e.getMessage());
        }
    }
}