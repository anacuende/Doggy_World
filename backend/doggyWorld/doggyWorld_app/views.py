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
            user = Usuario.objects.get(nombreUsuario = usernameEmail)
        except Usuario.DoesNotExist:
            try:
                user = Usuario.objects.get(email = usernameEmail)
            except Usuario.DoesNotExist:
                return JsonResponse({'error': 'Usuario no encontrado'}, status=404)
            
        if hashlib.sha384(password.encode()).hexdigest() == user.contrasena:
            token = secrets.token_hex(16)
            user.token = token
            user.save()
            return JsonResponse({'token': token}, status=201)
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
