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
        
        // Getters
        public int getId() { return id; }
        public String getNombre() { return nombre; }
        public double getPrecio() { return precio; }
        
        @Override
        public String toString() {
            return "Producto{id=" + id + ", nombre='" + nombre + "', precio=" + precio + "}";
        }
    }
    

    private static final String[] NOMBRES = {
        "Andres", "Camila", "Natalia", "Leonardo", "Ivan", "Liliana", "Omar"
    };
    
   
    private static final String[] APELLIDOS = {
        "Sierra", "Castañeda", "Orozco", "Rojas", "Medina", "Acosta", "Parra"
    };
    

    private static final String[] PRODUCTOS = {
        "Portatil", "Celular", "Televisor", "Auriculares", "Teclado USB", "Mouse usb", "Router"
    };
    

    private static final double[] PRECIOS = {
        2800000, 750000, 1280000, 50000, 40000, 21000, 150000
    };
    

    private static final Random random = new Random();

   
    public static void createSalesMenFile(int randomSalesCount, String name, long id) {
        String fileName = "ventas_" + id + ".txt";
        
        try (FileWriter writer = new FileWriter(fileName)) {
          
            writer.write("CC;" + id + "\n");
            
       
            for (int i = 0; i < randomSalesCount; i++) {
                int productId = 100 + random.nextInt(7);  
                int cantidad = random.nextInt(10) + 1;    
                writer.write(productId + ";" + cantidad + ";\n");
            }
            
            System.out.println("Archivo de ventas creado: " + fileName);
            
        } catch (IOException e) {
            System.err.println("Error al crear archivo de ventas: " + e.getMessage());
        }
    }

  
    public static void createProductsFile(int productsCount) {
        String fileName = "productos.txt";
        
        try (FileWriter writer = new FileWriter(fileName)) {
 
            for (int i = 0; i < productsCount; i++) {
                int productId = 100 + i;
                String productName = PRODUCTOS[i];
                double price = PRECIOS[i];
                

                writer.write(productId + ";" + productName + ";" + String.format("%.0f", price) + "\n");
            }
            
            System.out.println("Archivo de productos creado: " + fileName);
            
        } catch (IOException e) {
            System.err.println("Error al crear archivo de productos: " + e.getMessage());
        }
    }


    public static void createSalesManInfoFile(int salesmanCount) {
        String fileName = "vendedores.txt";
        
        try (FileWriter writer = new FileWriter(fileName)) {

            for (int i = 0; i < salesmanCount; i++) {
                String tipoDocumento = "CC";
                long numeroDocumento = 1000000000L + i;
                String nombre = NOMBRES[random.nextInt(NOMBRES.length)];
                String apellido = APELLIDOS[random.nextInt(APELLIDOS.length)];
                

                writer.write(tipoDocumento + ";" + numeroDocumento + ";" + nombre + ";" + apellido + "\n");
                

                int numeroVentas = random.nextInt(5) + 1; 
                createSalesMenFile(numeroVentas, nombre + "_" + apellido, numeroDocumento);
            }
            
            System.out.println("Archivo de vendedores creado: " + fileName);
            
        } catch (IOException e) {
            System.err.println("Error al crear archivo de vendedores: " + e.getMessage());
        }
    }


    public static void createSerializedProductsFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("productos_serializados.dat"))) {
            List<Producto> productos = new ArrayList<>();
            for (int i = 0; i < PRODUCTOS.length; i++) {
                productos.add(new Producto(100 + i, PRODUCTOS[i], PRECIOS[i]));
            }
            oos.writeObject(productos);
            System.out.println("Archivo serializado de productos creado: productos_serializados.dat");
        } catch (IOException e) {
            System.err.println("Error al crear archivo serializado: " + e.getMessage());
        }
    }


    public static void main(String[] args) {
        System.out.println("=== INICIANDO GENERACIÓN DE ARCHIVOS DE PRUEBA ===");
        
        try {
            // Generar archivo de productos primero
            createProductsFile(7);
            
            // Generar archivo de vendedores y sus ventas
            createSalesManInfoFile(3);
            
            // Generar archivo serializado (opcional)
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