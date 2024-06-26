"""doggyWorld URL Configuration

The `urlpatterns` list routes URLs to views. For more information please see:
    https://docs.djangoproject.com/en/3.2/topics/http/urls/
Examples:
Function views
    1. Add an import:  from my_app import views
    2. Add a URL to urlpatterns:  path('', views.home, name='home')
Class-based views
    1. Add an import:  from other_app.views import Home
    2. Add a URL to urlpatterns:  path('', Home.as_view(), name='home')
Including another URLconf
    1. Import the include() function: from django.urls import include, path
    2. Add a URL to urlpatterns:  path('blog/', include('blog.urls'))
"""
from django.contrib import admin
from django.urls import path
from doggyWorld_app import views

urlpatterns = [
    path('admin/', admin.site.urls),
    path('api/doggyWorld/session', views.session),
    path('api/doggyWorld/user/register', views.register_user),
    path('api/doggyWorld/user', views.update_user),
    path('api/doggyWorld/products', views.products),
    path('api/doggyWorld/products/<int:productId>', views.product_detail),
    path('api/doggyWorld/cart', views.cart),
    path('api/doggyWorld/wishlist', views.wishlist),
    path('api/doggyWorld/pedidos', views.pedidos),
]
