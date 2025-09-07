import java.io.*;
import java.util.Random;

public class GenerateInfoFiles {
    private static final String[] NOMBRES = {"Andres", "Camila", "Natalia", "Leonardo", "Ivan", "Liliana", "Omar"};
    private static final String[] APELLIDOS = {"Sierra", "Castañeda", "Orozco", "Rojas", "Medina", "Acosta", "Parra"};
    private static final String[] PRODUCTOS = {"Portatil", "Celular", "Televisor", "Auriculares", "Teclado USB", "Mouse usb", "Router"};
    private static final double[] PRECIOS = {2800000, 750000, 1280000, 50000, 40000, 21000, 150000};
    private static final Random random = new Random();

    public static void createSalesMenFile(int randomSalesCount, String name, long id) {
        try (FileWriter writer = new FileWriter("ventas_" + id + ".txt")) {
            writer.write("CC;" + id + "\n");
            
            for (int i = 0; i < randomSalesCount; i++) {
                int productId = 100 + random.nextInt(7);
                int cantidad = random.nextInt(10) + 1;
                writer.write(productId + ";" + cantidad + "\n");
            }
            System.out.println("Archivo ventas_" + id + ".txt creado");
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void createProductsFile(int productsCount) {
        try (FileWriter writer = new FileWriter("productos.txt")) {
            for (int i = 0; i < productsCount; i++) {
            	writer.write((100 + i) + ";" + PRODUCTOS[i] + ";" + String.format("%.0f", PRECIOS[i]) + "\n");
            }
            System.out.println("Archivo productos.txt creado");
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void createSalesManInfoFile(int salesmanCount) {
        try (FileWriter writer = new FileWriter("vendedores.txt")) {
            for (int i = 0; i < salesmanCount; i++) {
                long documento = 1000000000L + i;
                String nombre = NOMBRES[random.nextInt(NOMBRES.length)];
                String apellido = APELLIDOS[random.nextInt(APELLIDOS.length)];
                
                writer.write("CC;" + documento + ";" + nombre + ";" + apellido + "\n");
                createSalesMenFile(random.nextInt(5) + 1, nombre + "_" + apellido, documento);
            }
            System.out.println("Archivo vendedores.txt creado");
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        System.out.println("=== GENERANDO ARCHIVOS DE PRUEBA ===");
        createProductsFile(7);
        createSalesManInfoFile(3);
        System.out.println("=== GENERACIÓN COMPLETADA ===");
    }
}