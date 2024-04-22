from django.shortcuts import render
from .models import Usuario, Pedido, Producto, DetallePedido, Carrito, ListaDeseos
from django.views.decorators.csrf import csrf_exempt
from django.http import JsonResponse
import json, secrets, hashlib

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