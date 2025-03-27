# Pre-parcial-Ciclos
Voy a explicarte paso a paso cómo:  

1. **Crear una base de datos en MongoDB**  
2. **Conectar la base de datos con Spring Boot**  
3. **Exponer endpoints en Spring Boot**  
4. **Conectar el backend con el frontend en React**  

---  

## **1. Crear una base de datos en MongoDB**  

Como usas **MongoDB**, puedes crear la base de datos de dos formas:  

### **Opción 1: Usar MongoDB Atlas (nube)**  
1. Crea una cuenta en [MongoDB Atlas](https://www.mongodb.com/atlas).  
2. Crea un **Cluster** y una base de datos (ejemplo: `inventarioDB`).  
3. En **Database Access**, agrega un usuario con permisos de lectura y escritura.  
4. En **Network Access**, permite el acceso desde tu IP.  
5. Copia la **URI de conexión** (`mongodb+srv://usuario:password@cluster.mongodb.net/inventarioDB`).  

### **Opción 2: Instalar MongoDB localmente**  
1. Descarga e instala [MongoDB](https://www.mongodb.com/try/download/community).  
2. Inicia el servicio con:  
   ```sh
   mongod --dbpath /ruta/donde/guardar/los/datos
   ```  
3. Usa el shell de Mongo para crear una base de datos:  
   ```sh
   mongo
   use inventarioDB
   ```

---  

## **2. Conectar MongoDB con Spring Boot**  

1. **Agrega las dependencias en `pom.xml`**  
   ```xml
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-data-mongodb</artifactId>
   </dependency>
   ```

2. **Configura la conexión en `application.properties` o `application.yml`**  

   - Si usas **MongoDB Atlas**:  
     ```properties
     spring.data.mongodb.uri=mongodb+srv://usuario:password@cluster.mongodb.net/inventarioDB
     ```

   - Si usas **MongoDB local**:  
     ```properties
     spring.data.mongodb.host=localhost
     spring.data.mongodb.port=27017
     spring.data.mongodb.database=inventarioDB
     ```

3. **Crea un modelo para los datos (`Producto.java`)**  
   ```java
   import org.springframework.data.annotation.Id;
   import org.springframework.data.mongodb.core.mapping.Document;

   @Document(collection = "productos")
   public class Producto {
       @Id
       private String id;
       private String nombre;
       private double precio;
       private int stock;

       // Getters y Setters
   }
   ```

4. **Crea un repositorio para acceder a los datos (`ProductoRepository.java`)**  
   ```java
   import org.springframework.data.mongodb.repository.MongoRepository;

   public interface ProductoRepository extends MongoRepository<Producto, String> {
   }
   ```

5. **Crea un servicio para la lógica de negocio (`ProductoService.java`)**  
   ```java
   import org.springframework.stereotype.Service;
   import java.util.List;

   @Service
   public class ProductoService {
       private final ProductoRepository repository;

       public ProductoService(ProductoRepository repository) {
           this.repository = repository;
       }

       public List<Producto> listar() {
           return repository.findAll();
       }

       public Producto guardar(Producto producto) {
           return repository.save(producto);
       }
   }
   ```

6. **Crea un controlador REST para exponer los endpoints (`ProductoController.java`)**  
   ```java
   import org.springframework.web.bind.annotation.*;

   import java.util.List;

   @RestController
   @RequestMapping("/productos")
   public class ProductoController {
       private final ProductoService service;

       public ProductoController(ProductoService service) {
           this.service = service;
       }

       @GetMapping
       public List<Producto> listar() {
           return service.listar();
       }

       @PostMapping
       public Producto agregar(@RequestBody Producto producto) {
           return service.guardar(producto);
       }
   }
   ```

---  

## **3. Conectar el backend con React**  

### **A. Configurar CORS en Spring Boot**  
Como el frontend estará en otro repositorio, debes permitir solicitudes desde React agregando esto en `ProductoController.java`:  
```java
@CrossOrigin(origins = "http://localhost:3000")
```
o una configuración global en Spring Boot:  
```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**").allowedOrigins("http://localhost:3000");
            }
        };
    }
}
```

### **B. Crear el frontend en React**  
1. **Crear el proyecto React**  
   ```sh
   npx create-react-app inventario-frontend
   cd inventario-frontend
   npm install axios
   ```

2. **Configurar el acceso al backend**  
   - Crea un archivo `services/api.js` con Axios:  
     ```javascript
     import axios from 'axios';

     const API_URL = 'http://localhost:8080/productos';

     export const obtenerProductos = async () => {
         const response = await axios.get(API_URL);
         return response.data;
     };

     export const agregarProducto = async (producto) => {
         const response = await axios.post(API_URL, producto);
         return response.data;
     };
     ```

3. **Mostrar los productos en React (`App.js`)**  
   ```javascript
   import React, { useEffect, useState } from 'react';
   import { obtenerProductos, agregarProducto } from './services/api';

   function App() {
       const [productos, setProductos] = useState([]);
       const [nuevoProducto, setNuevoProducto] = useState({ nombre: '', precio: '', stock: '' });

       useEffect(() => {
           obtenerProductos().then(setProductos);
       }, []);

       const handleSubmit = async (e) => {
           e.preventDefault();
           const producto = await agregarProducto(nuevoProducto);
           setProductos([...productos, producto]);
       };

       return (
           <div>
               <h1>Gestión de Inventario</h1>
               <form onSubmit={handleSubmit}>
                   <input type="text" placeholder="Nombre" onChange={(e) => setNuevoProducto({ ...nuevoProducto, nombre: e.target.value })} />
                   <input type="number" placeholder="Precio" onChange={(e) => setNuevoProducto({ ...nuevoProducto, precio: e.target.value })} />
                   <input type="number" placeholder="Stock" onChange={(e) => setNuevoProducto({ ...nuevoProducto, stock: e.target.value })} />
                   <button type="submit">Agregar</button>
               </form>
               <ul>
                   {productos.map((p) => (
                       <li key={p.id}>{p.nombre} - ${p.precio} - Stock: {p.stock}</li>
                   ))}
               </ul>
           </div>
       );
   }

   export default App;
   ```

### **C. Ejecutar la aplicación**  
- **Backend**  
  ```sh
  mvn spring-boot:run
  ```
- **Frontend**  
  ```sh
  npm start
  ```

---

## **Conclusión**  
- Creaste una **base de datos en MongoDB** (local o en Atlas).  
- Configuraste **Spring Boot para conectarse a MongoDB**.  
- Exponiste una API con **Spring Boot**.  
- Conectaste React con Spring Boot usando **Axios**.  

Con esto, tu aplicación de inventario debería estar funcionando 🚀.

Para desplegar tu aplicación en **Azure**, debes considerar que tienes dos componentes:  

1. **Backend con Spring Boot + MongoDB**  
2. **Frontend con React**  

Voy a explicarte cómo desplegar ambos en **Azure** utilizando **Azure App Service** para el backend y **Azure Static Web Apps** para el frontend.

---

# **1. Desplegar el Backend en Azure App Service**
## **A. Crear el servicio en Azure**
1. Ve a [Azure Portal](https://portal.azure.com).
2. Busca **App Services** y haz clic en **Crear**.
3. Llena los datos:
   - **Nombre**: `inventario-backend`
   - **Pila de ejecución**: **Java 17** (o la versión que uses)
   - **Región**: Elige una cercana a tus usuarios.
   - **Plan de precios**: Usa **B1 (Básico)** si es una prueba.
4. Haz clic en **Revisar y Crear** → **Crear**.

---

## **B. Configurar MongoDB en Azure**
Si usas **MongoDB Atlas**, solo asegúrate de que tu base de datos está permitiendo conexiones desde Azure.

Si prefieres **MongoDB en Azure**, usa **Azure Cosmos DB para MongoDB**:

1. En el portal de Azure, busca **Cosmos DB** y crea una nueva instancia.
2. Selecciona la opción **API de MongoDB**.
3. Copia la **URI de conexión** y agrégala a tu `application.properties`:

   ```properties
   spring.data.mongodb.uri=mongodb+srv://usuario:password@tu-cluster.mongodb.net/inventarioDB
   ```

---

## **C. Configurar GitHub Actions para el Backend**
1. Asegúrate de que tu código esté en **GitHub**.
2. Ve a **App Services** en Azure, selecciona tu aplicación y busca **Centro de implementación**.
3. Conéctalo con tu repositorio en GitHub.
4. Azure generará un archivo **GitHub Actions** en `.github/workflows/azure.yml`. Si no, crea uno manualmente:

   ```yaml
   name: Deploy Backend to Azure

   on:
     push:
       branches:
         - main

   jobs:
     build:
       runs-on: ubuntu-latest
       steps:
         - name: Checkout code
           uses: actions/checkout@v2

         - name: Set up JDK 17
           uses: actions/setup-java@v2
           with:
             distribution: 'temurin'
             java-version: '17'

         - name: Build project with Maven
           run: mvn clean package

         - name: Deploy to Azure
           uses: azure/webapps-deploy@v2
           with:
             app-name: 'inventario-backend'
             slot-name: 'production'
             package: target/*.jar
   ```

5. Confirma los cambios y haz un `push` a `main`. GitHub Actions se encargará del despliegue.

---

## **D. Configurar variables de entorno en Azure**
1. Ve a **Configuración > Configuración de la aplicación**.
2. Agrega la variable:
   - **spring.data.mongodb.uri** → `mongodb+srv://usuario:password@tu-cluster.mongodb.net/inventarioDB`
3. Reinicia la aplicación.

Tu backend ya está disponible en **https://inventario-backend.azurewebsites.net** 🎉.

---

# **2. Desplegar el Frontend en Azure Static Web Apps**
## **A. Crear la aplicación en Azure**
1. En [Azure Portal](https://portal.azure.com), busca **Static Web Apps** y haz clic en **Crear**.
2. Elige el repositorio de GitHub donde está tu frontend.
3. Configura:
   - **Nombre de la aplicación**: `inventario-frontend`
   - **Región**: La más cercana a tus usuarios.
   - **Framework**: **React**
4. Haz clic en **Revisar y Crear** → **Crear**.

---

## **B. Configurar GitHub Actions para el Frontend**
Azure generará automáticamente un archivo en `.github/workflows/azure-static-web-apps.yml`. Si no, crea uno manualmente:

```yaml
name: Deploy Frontend to Azure

on:
  push:
    branches:
      - main

jobs:
  build_and_deploy_job:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout repository
        uses: actions/checkout@v2

      - name: Install dependencies
        run: npm install

      - name: Build project
        run: npm run build

      - name: Deploy to Azure Static Web Apps
        uses: Azure/static-web-apps-deploy@v1
        with:
          azure_static_web_apps_api_token: ${{ secrets.AZURE_STATIC_WEB_APPS_API_TOKEN }}
          repo_token: ${{ secrets.GITHUB_TOKEN }}
          action: "upload"
          app_location: "/"
          output_location: "build"
```

Haz un **push** a `main` y GitHub Actions hará el despliegue.

---

## **C. Configurar conexión entre el Frontend y Backend**
Tu backend estará en `https://inventario-backend.azurewebsites.net`, así que en **React** modifica `services/api.js`:

```javascript
const API_URL = "https://inventario-backend.azurewebsites.net/productos";
```

Haz `push` y el frontend se actualizará automáticamente 🚀.

---

# **Resumen Final**
✅ **Backend en Azure App Service**  
✅ **MongoDB en MongoDB Atlas o Azure Cosmos DB**  
✅ **Frontend en Azure Static Web Apps**  
✅ **Despliegue automático con GitHub Actions**  

¡Listo! Tu aplicación está en producción en **Azure** 🎉.
