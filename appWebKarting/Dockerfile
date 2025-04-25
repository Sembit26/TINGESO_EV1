FROM nginx:alpine

# Copia tus archivos estáticos del build (Angular, React, etc.)
COPY dist/ /usr/share/nginx/html/


# Comando para mantener NGINX ejecutándose en primer plano
CMD ["nginx", "-g", "daemon off;"]
