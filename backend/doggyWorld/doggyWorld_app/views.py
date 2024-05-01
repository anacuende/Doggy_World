from django.shortcuts import render
from .models import Usuario, Pedido, Producto, DetallePedido, Carrito, ListaDeseos
from django.views.decorators.csrf import csrf_exempt
from django.http import JsonResponse
import json, secrets, hashlib, random

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
    if request.method == 'POST':
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
    else:
        return JsonResponse({"error": "Error interno de servidor"}, status=500)