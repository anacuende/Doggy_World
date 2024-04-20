from django.db import models

from django.db import models

class Usuario(models.Model):
    nombre = models.CharField(max_length=100)
    nombreUsuario = models.CharField(max_length=200)
    email = models.EmailField(unique=True)
    contrasena = models.CharField(max_length=150)
    token = models.CharField(max_length=40,null=True)

class Pedido(models.Model):
    fecha = models.DateField()
    direccion = models.CharField(max_length=200)
    localidad = models.CharField(max_length=200)
    pais = models.CharField(max_length=200)
    titularTarjeta = models.CharField(max_length=100)
    numTarjeta = models.CharField(max_length=16)
    cadTarjeta = models.CharField(max_length=5)
    CVV = models.IntegerField()
    precioTotal = models.DecimalField(max_digits=10, decimal_places=2)
    id_usuario = models.ForeignKey(Usuario, on_delete=models.CASCADE)

class Producto(models.Model):
    nombre = models.CharField(max_length=300)
    precio = models.DecimalField(max_digits=10, decimal_places=2)
    descripcion = models.CharField(max_length=2000)
    imagen = models.URLField()
    categoria = models.IntegerField()
    stock = models.IntegerField(default=0)

class DetallePedido(models.Model):
    id_pedido = models.ForeignKey(Pedido, on_delete=models.CASCADE)
    id_producto = models.ForeignKey(Producto, on_delete=models.CASCADE)
    cantidadProducto = models.IntegerField()

class Carrito(models.Model):
    cantidad = models.IntegerField()
    id_producto = models.ForeignKey(Producto, on_delete=models.CASCADE)
    id_usuario = models.ForeignKey(Usuario, on_delete=models.CASCADE)

class ListaDeseos(models.Model):
    id_producto = models.ForeignKey(Producto, on_delete=models.CASCADE)
    id_usuario = models.ForeignKey(Usuario, on_delete=models.CASCADE)
