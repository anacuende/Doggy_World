from django.shortcuts import render
from .models import Usuario, Pedido, Producto, DetallePedido, Carrito, ListaDeseos
from django.views.decorators.csrf import csrf_exempt
from django.http import JsonResponse
from django.db import transaction
import json, secrets, hashlib, random, datetime

@csrf_exempt
def session(request):
    if request.method == 'POST':
        body_json = json.loads(request.body)

        if 'user' not in body_json or 'password' not in body_json:
            return JsonResponse({'error': 'Faltan parámetros o parámetros incorrectos'}, status=400)
        
        usernameEmail = body_json['user']
        password = body_json['password']

        try:
            user = Usuario.objects.get(email = usernameEmail) if '@' in usernameEmail else Usuario.objects.get(nombreUsuario = usernameEmail)
        except Usuario.DoesNotExist:
            return JsonResponse({'error': 'Usuario no encontrado'}, status=404)
            
        if hashlib.sha384(password.encode()).hexdigest() == user.contrasena:
            token = secrets.token_hex(16)
            user.token = token
            user.save()
            return JsonResponse({'message': 'Sesión iniciada'}, status=201)
        else:
            return JsonResponse({'error': 'Contraseña incorrecta'}, status=401)
    
    elif request.method == 'DELETE':
        try:
            token = request.headers.get('token')
        except:
            return JsonResponse({'error': 'Token no existente'}, status=400)
        
        if token is None:
            return JsonResponse({'error': 'Token nulo'}, status=400)
        
        if not Usuario.objects.filter(token = token).exists():
            return JsonResponse({'error': 'Usuario no encontrado'}, status=404)
    
        user = Usuario.objects.get(token = token)
        user.token = ""
        user.save()

        return JsonResponse({'message': 'Sesión cerrada'}, status=200)
    else:
        return JsonResponse({'error': 'Error interno de servidor'}, status=500)

@csrf_exempt
def register_user(request):
    if request.method == 'GET':
        try:
            token = request.headers.get("token")
        except:
            return JsonResponse({"error": "Token no proporcionado"}, status=400)
        
        if not Usuario.objects.filter(token=token).exists():
            return JsonResponse({"error": "Token no encontrado"}, status=404)

        user = Usuario.objects.get(token=token)

        user_info = {
            'id': user.id,
            'name': user.nombre,
            'username': user.nombreUsuario,
            'email': user.email
        }

        return JsonResponse(user_info, status=200)

    elif request.method == 'POST':
        body_json = json.loads(request.body)

        campos_requeridos = ['name', 'username', 'email', 'password', 'confirm_password']
        for campo in campos_requeridos:
            if campo not in body_json:
                return JsonResponse({'error': 'Falta el campo requerido: {campo}'}, status=404)

        email = body_json['email']
        if not '@' in email:
            return JsonResponse({'error': 'El email no es válido'}, status=400)

        if Usuario.objects.filter(email=email).exists():
            return JsonResponse({'error': 'El email ya está registrado'}, status=400)

        password = body_json['password']
        confirm_password = body_json['confirm_password']
        if password != confirm_password:
            return JsonResponse({'error': 'Las contraseñas no coinciden'}, status=400)

        token = secrets.token_hex(16)

        try:
            user = Usuario.objects.create(
                nombre=body_json['name'],
                nombreUsuario=body_json['username'],
                email=body_json['email'],
                contrasena=hashlib.sha384(password.encode()).hexdigest(),
                token=token
            )
            user.save()
        except Exception as e:
            return JsonResponse({'error': 'No se pudo crear el usuario'}, status=500)

        return JsonResponse({'message': 'Usuario registrado correctamente'}, status=201)
    else:
        return JsonResponse({'error': 'Error interno de servidor'}, status=500)

@csrf_exempt
def update_user(request):
    if request.method == 'PATCH':
        token = request.headers.get('token')

        if not token:
            return JsonResponse({'error': 'Token no existente'}, status=404)
        
        try:
            user = Usuario.objects.get(token=token)
        except Usuario.DoesNotExist:
            return JsonResponse({'error': 'Usuario no encontrado'}, status=404)

        body_json = json.loads(request.body)        

        if 'password' not in body_json or 'confirmPassword' not in body_json:
            return JsonResponse({'error': 'Faltan campos de contraseña'}, status=400)

        if body_json['password'] != body_json['confirmPassword']:
            return JsonResponse({'error': 'Las contraseñas no coinciden'}, status=400)

        if 'email' in body_json:
            if not '@' in 'email':
                return JsonResponse({'error': 'El email no es válido'}, status=400)
            user.email = body_json['email']
        if 'name' in body_json:
            user.nombre = body_json['name']
        if 'username' in body_json:
            user.nombreUsuario = body_json['username']
        user.contrasena = body_json['password']

        user.save()

        return JsonResponse({'message': 'Usuario actualizado correctamente'}, status=200)
    else:
        return JsonResponse({'error': 'Error interno de servidor'}, status=500)

@csrf_exempt
def products(request):
    if request.method == "GET":
        try:
            token = request.headers.get("token")
        except:
            return JsonResponse({"error": "Token no proporcionado"}, status=400)
        
        if not Usuario.objects.filter(token=token).exists():
            return JsonResponse({"error": "Token no encontrado"}, status=404)

        categoria_id = request.GET.get("category")
        cantidad_aleatoria = request.GET.get("random")

        productos = Producto.objects.all()

        if categoria_id:
            cat_product = int(categoria_id)
            productos = Producto.objects.filter(categoria=cat_product)
        elif cantidad_aleatoria:
            random_elements = int(cantidad_aleatoria)
            productos = random.sample(list(Producto.objects.all()), random_elements)
        else:
            productos = Producto.objects.all()

        json_data = []
        for producto in productos:
            json_data.append({
                'id': producto.id,
                'nombre': producto.nombre,
                'descripcion': producto.descripcion,
                'precio': float(producto.precio),
                'imagen': producto.imagen
            })

        return JsonResponse(json_data, status=200, safe=False)
    else:
        return JsonResponse({"error": "Error interno de servidor"}, status=500)

@csrf_exempt
def product_detail(request, productId):
    if request.method == "GET":
        try:
            token = request.headers.get("token")
        except:
            return JsonResponse({"error": "Token no proporcionado"}, status=400)
        
        if not Usuario.objects.filter(token=token).exists():
            return JsonResponse({"error": "Token no encontrado"}, status=404)

        try:
            producto = Producto.objects.get(id=productId)
        except Producto.DoesNotExist:
            return JsonResponse({"error": "Producto no encontrado"}, status=400)
        
        json_data = {
            'id': producto.id,
            'nombre': producto.nombre,
            'descripcion': producto.descripcion,
            'precio': float(producto.precio),
            'imagen': producto.imagen
        }
        
        return JsonResponse(json_data, status=200)
    
    else:
        return JsonResponse({"error": "Error interno de servidor"}, status=500)
    
@csrf_exempt
def cart(request):
    if request.method == "GET":
        try:
            token = request.headers.get("token")
        except:
            return JsonResponse({"error": "Token no proporcionado"}, status=400)

        if not Usuario.objects.filter(token=token).exists():
            return JsonResponse({"error": "Token no encontrado"}, status=404)

        user = Usuario.objects.get(token=token)
        cart_products = Carrito.objects.filter(id_usuario=user)

        if cart_products:
            products_data = []
            for cart_product in cart_products:
                product_data = {
                    'id': cart_product.id_producto.id,
                    'nombre': cart_product.id_producto.nombre,
                    'precio': cart_product.id_producto.precio,
                    'imagen': cart_product.id_producto.imagen,
                    'categoria': cart_product.id_producto.categoria,
                    'stock': cart_product.id_producto.stock,
                    'cantidad': cart_product.cantidad
                }
                products_data.append(product_data)
            return JsonResponse(products_data, status=200, safe=False)
        else:
            return JsonResponse({"message": "El carrito está vacío"}, status=204)
    
    elif request.method == "POST":
        try:
            token = request.headers.get("token")
        except:
            return JsonResponse({"error": "Token no proporcionado"}, status=400)
        
        if not Usuario.objects.filter(token=token).exists():
            return JsonResponse({"error": "Token no encontrado"}, status=404)

        try:
            body_json = json.loads(request.body)
            producto_id = body_json.get("producto_id")
            cantidad = body_json.get("cantidad")
        except:
            return JsonResponse({"error": "Datos no proporcionados o inválidos"}, status=400)

        if not (producto_id and cantidad):
            return JsonResponse({"error": "Datos faltantes"}, status=400)

        if not Producto.objects.filter(id=producto_id).exists():
            return JsonResponse({"error": "Producto no encontrado"}, status=404)

        usuario = Usuario.objects.get(token=token)
        producto = Producto.objects.get(id=producto_id)

        carrito, created = Carrito.objects.get_or_create(
            id_usuario=usuario,
            id_producto=producto,
            defaults={"cantidad": cantidad}
        )

        if not created:
            carrito.cantidad += cantidad
            carrito.save()

        return JsonResponse({"message": "Producto agregado al carrito de compras correctamente"}, status=201)

    elif request.method == "DELETE":
        try:
            token = request.headers.get("token")
        except:
            return JsonResponse({"error": "Token no proporcionado"}, status=400)

        if not Usuario.objects.filter(token=token).exists():
            return JsonResponse({"error": "Token no encontrado"}, status=404)

        user = Usuario.objects.get(token=token)

        producto_id = request.GET.get("productId")

        if not producto_id:
            return JsonResponse({"error": "ID del producto no proporcionado"}, status=400)

        try:
            carrito_producto = Carrito.objects.get(id_usuario=user, id_producto=producto_id)
        except Carrito.DoesNotExist:
            return JsonResponse({"error": "El producto no está en el carrito del usuario"}, status=400)

        carrito_producto.delete()

        return JsonResponse({"message": "Producto eliminado del carrito correctamente"}, status=200)

    else:
        return JsonResponse({"error": "Error interno de servidor"}, status=500)
    
@csrf_exempt
def wishlist(request):
    if request.method == "GET":
        try:
            token = request.headers.get("token")
        except:
            return JsonResponse({"error": "Token no proporcionado"}, status=400)

        if not Usuario.objects.filter(token=token).exists():
            return JsonResponse({"error": "Token no encontrado"}, status=404)

        user = Usuario.objects.get(token=token)
        wishlist_products = ListaDeseos.objects.filter(id_usuario=user)

        if wishlist_products:
            products_data = []
            for wishlist_product in wishlist_products:
                product_data = {
                    'id': wishlist_product.id_producto.id,
                    'nombre': wishlist_product.id_producto.nombre,
                    'precio': wishlist_product.id_producto.precio,
                    'imagen': wishlist_product.id_producto.imagen,
                    'descripcion': wishlist_product.id_producto.descripcion
                }
                products_data.append(product_data)
            return JsonResponse(products_data, status=200, safe=False)
        else:
            return JsonResponse({"message": "La lista de deseos está vacía"}, status=204)
        
    elif request.method == "POST":
        try:
            token = request.headers.get("token")
        except:
            return JsonResponse({"error": "Token no proporcionado"}, status=400)
        
        if not Usuario.objects.filter(token=token).exists():
            return JsonResponse({"error": "Token no encontrado"}, status=404)

        try:
            body_json = json.loads(request.body)
            producto_id = body_json.get("producto_id")
        except:
            return JsonResponse({"error": "Datos no proporcionados o inválidos"}, status=400)

        if not producto_id:
            return JsonResponse({"error": "ID del producto no proporcionado"}, status=400)

        if not Producto.objects.filter(id=producto_id).exists():
            return JsonResponse({"error": "Producto no encontrado"}, status=404)

        usuario = Usuario.objects.get(token=token)
        producto = Producto.objects.get(id=producto_id)

        wishlist, created = ListaDeseos.objects.get_or_create(
            id_usuario=usuario,
            id_producto=producto
        )

        if created:
            return JsonResponse({"message": "Producto agregado a la lista de deseos correctamente"}, status=201)
        else:
            return JsonResponse({"message": "El producto ya está en la lista de deseos"}, status=400)

    elif request.method == "DELETE":
        try:
            token = request.headers.get("token")
        except:
            return JsonResponse({"error": "Token no proporcionado"}, status=400)
        
        if not Usuario.objects.filter(token=token).exists():
            return JsonResponse({"error": "Token no encontrado"}, status=404)

        usuario = Usuario.objects.get(token=token)

        producto_id = request.GET.get("productId")

        if not producto_id:
            return JsonResponse({"error": "ID del producto no proporcionado"}, status=400)

        try:
            lista_deseos_producto = ListaDeseos.objects.get(id_usuario=usuario, id_producto=producto_id)
        except ListaDeseos.DoesNotExist:
            return JsonResponse({"error": "El producto no está en la lista de deseos del usuario"}, status=400)

        lista_deseos_producto.delete()

        return JsonResponse({"message": "Producto eliminado de la lista de deseos correctamente"}, status=200)
    
    else:
        return JsonResponse({"error": "Error interno de servidor"}, status=500)

@csrf_exempt
def pedidos(request):
    if request.method == "GET":
        try:
            token = request.headers.get("token")
        except:
            return JsonResponse({"error": "Token no proporcionado"}, status=400)
        
        if not Usuario.objects.filter(token=token).exists():
            return JsonResponse({"error": "Token no encontrado"}, status=404)

        pedidos = Pedido.objects.all()

        if pedidos:
            pedidos_data = []
            for pedido in pedidos:
                pedidos_data.append({
                    'id': pedido.id,
                    'fecha': pedido.fecha,
                    'direccion': pedido.direccion,
                    'localidad': pedido.localidad,
                    'pais': pedido.pais,
                    'titularTarjeta': pedido.titularTarjeta,
                    'numTarjeta': pedido.numTarjeta,
                    'cadTarjeta': pedido.cadTarjeta,
                    'CVV': pedido.CVV,
                    'precioTotal': pedido.precioTotal,
                })
            return JsonResponse(pedidos_data, status=200, safe=False)
        else:
            return JsonResponse({"message": "No hay pedidos disponibles"}, status=204)
    
    elif request.method == "POST":
        try:
            token = request.headers.get("token")
        except:
            return JsonResponse({"error": "Token no proporcionado"}, status=400)
        
        if not Usuario.objects.filter(token=token).exists():
            return JsonResponse({"error": "Token no encontrado"}, status=404)

        try:
            body_json = json.loads(request.body)
            direccion = body_json.get("direccion")
            localidad = body_json.get("localidad")
            pais = body_json.get("pais")
            titularTarjeta = body_json.get("titularTarjeta")
            numTarjeta = body_json.get("numTarjeta")
            cadTarjeta = body_json.get("cadTarjeta")
            CVV = body_json.get("CVV")
        except:
            return JsonResponse({"error": "Datos no proporcionados o inválidos"}, status=400)

        user = Usuario.objects.get(token=token)

        productos_en_carrito = Carrito.objects.filter(id_usuario=user)
        if not productos_en_carrito:
            return JsonResponse({"error": "No hay productos en el carrito"}, status=400)

        try:
            precio_total = sum(producto_carrito.id_producto.precio * producto_carrito.cantidad for producto_carrito in productos_en_carrito)

            pedido = Pedido.objects.create(
                fecha = datetime.date.today(),
                direccion = direccion,
                localidad = localidad,
                pais = pais,
                titularTarjeta = titularTarjeta,
                numTarjeta = numTarjeta,
                cadTarjeta = cadTarjeta,
                CVV = CVV,
                precioTotal = precio_total,
                id_usuario = user
            )

            pedido.save()

            for producto_carrito in productos_en_carrito:
                producto = producto_carrito.id_producto
    
                if producto.stock <= producto_carrito.cantidad:
                    return JsonResponse({"error": "No hay suficiente stock"}, status=404)
                producto.stock -= producto_carrito.cantidad

                producto.save()
                DetallePedido.objects.create(
                    id_pedido=pedido,
                    id_producto=producto,
                    cantidadProducto=producto_carrito.cantidad
                )

            pedido.precioTotal = precio_total
            
            productos_en_carrito.delete()
                
        except Exception as e:
            return JsonResponse({"error": "Error al procesar el pedido"}, status=500)

        return JsonResponse({"message": "Pedido realizado correctamente"}, status=201)
    
    elif request.method == "DELETE":
        try:
            token = request.headers.get("token")
        except:
            return JsonResponse({"error": "Token no proporcionado"}, status=400)
        
        if not Usuario.objects.filter(token=token).exists():
            return JsonResponse({"error": "Token no encontrado"}, status=404)

        try:
            pedido_id = request.GET.get("pedidoId")
            pedido = Pedido.objects.get(id=pedido_id)
        except Pedido.DoesNotExist:
            return JsonResponse({"error": "Pedido no encontrado"}, status=404)

        try:
            detalles_pedido = pedido.detallepedido_set.all()
            for detalle in detalles_pedido:
                producto = detalle.id_producto
                producto.stock += detalle.cantidadProducto
                producto.save()

            pedido.delete()
        except Exception as e:
            return JsonResponse({"error": "Error al cancelar el pedido"}, status=500)

        return JsonResponse({"message": "Pedido cancelado correctamente"}, status=200)
    else:
        return JsonResponse({"error": "Error interno de servidor"}, status=500)