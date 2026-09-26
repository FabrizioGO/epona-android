---
title: Epona — Política de Privacidad
layout: default
---

# Política de Privacidad

**Última actualización: {{ site.terms_version }}**

Esta Política de Privacidad explica cómo Epona ("**Epona**", "**nosotros**") recopila, usa y comparte información cuando usas la aplicación móvil Epona (la "**App**").

Epona es operada por {{ site.legal_name }}, una persona natural con domicilio en {{ site.governing_country }}, responsable del tratamiento de datos para efectos de esta política.

Si no estás de acuerdo con esta política, por favor no uses la App.

## 1. Información que recopilamos

### Información de la cuenta
Al crear una cuenta recopilamos tu **nombre**, **correo electrónico** y, si defines una, una **foto de perfil**. Si inicias sesión con Google, recibimos el nombre, correo y foto de perfil que Google nos comparte.

### Información de mascotas
Las mascotas que registras incluyen: nombre, especie, raza, color, tamaño, edad aproximada, sexo, un número de microchip opcional, una descripción y hasta tres fotos.

### Información de alertas
Al publicar una alerta de mascota perdida o encontrada, recopilamos: la ubicación (coordenadas y dirección) donde fue vista por última vez, fecha y hora, una descripción, si ofreces recompensa, y un **número de teléfono de contacto**. Consulta [Qué pueden ver otros usuarios](#3-qué-pueden-ver-otros-usuarios) — esta información no es privada.

### Información de avistamientos
Al reportar que viste la mascota de otra persona, recopilamos: la ubicación y dirección donde la viste, hasta tres fotos, una nota opcional y la hora.

### Datos de ubicación
La App solicita la ubicación **precisa o aproximada** de tu dispositivo (solo en primer plano — nunca recopilamos ubicación en segundo plano). La usamos para:
- mostrarte alertas cercanas y calcular la distancia,
- asociar un punto de ubicación a una publicación que crees,
- opcionalmente guardar tu última ubicación conocida para notificarte sobre alertas cercanas.

Las fotos se procesan en tu dispositivo antes de subirse, lo que elimina cualquier dato de ubicación (EXIF) incrustado en el archivo.

### Notificaciones
Guardamos un token de notificaciones push (de Firebase Cloud Messaging) asociado a tu cuenta, y un historial de las notificaciones que te enviamos, para que la App pueda mostrar tu lista de notificaciones.

### Información recopilada automáticamente
Nuestro proveedor de infraestructura (Supabase) registra automáticamente datos técnicos estándar por seguridad y confiabilidad, como dirección IP, marcas de tiempo y metadatos de solicitudes. No usamos estos datos para publicidad ni perfilamiento.

### Lo que no recopilamos
No usamos SDKs de publicidad ni herramientas de analítica que te perfilen con fines publicitarios. No recopilamos ubicación en segundo plano. No solicitamos acceso a toda tu galería de fotos — eliges fotos individuales mediante el selector de fotos del sistema operativo, que nunca nos comparte el resto de tu biblioteca.

## 2. Cómo usamos tu información

Usamos tu información para:
- crear y proteger tu cuenta, y permitirte iniciar sesión;
- permitirte publicar y gestionar alertas y reportes de avistamiento;
- mostrarte alertas cercanas y calcular distancias;
- enviarte notificaciones push sobre actividad relevante (un avistamiento en tu alerta, una nueva alerta cercana, tu mascota marcada como reencontrada);
- mantener una caché local en tu dispositivo para que la App funcione sin conexión y cargue rápido;
- mantener la seguridad, integridad y correcto funcionamiento de la App;
- responder solicitudes de soporte y hacer cumplir nuestros [Términos de Servicio](terms.html).

## 3. Qué pueden ver otros usuarios

Epona es una app comunitaria basada en publicaciones públicas. Si publicas una alerta:
- tu **nombre visible**, **foto de perfil**, los **datos y fotos de la mascota**, la **ubicación/dirección** donde fue vista por última vez, y el **teléfono de contacto** que ingreses son visibles para **todos los demás usuarios** con sesión iniciada en la App, y los archivos de fotos son accesibles por cualquiera que tenga su enlace directo, use o no la App.
- Las alertas pueden compartirse fuera de la App (por ejemplo, mediante un enlace), lo que hace esa información visible más ampliamente todavía.
- Si reportas un avistamiento, tu **nombre** y **foto de perfil** son visibles para el dueño de la alerta y para cualquiera que vea el historial de avistamientos de esa alerta.

Por favor no incluyas en una descripción o nota información que no quieras que sea pública, y piensa bien antes de compartir un número de teléfono personal.

## 4. Compartir información y proveedores de servicio

No vendemos tu información personal, ni la compartimos con terceros para sus propios fines publicitarios.

Compartimos información con los siguientes proveedores de servicio, quienes la procesan en nuestro nombre bajo sus propios compromisos de seguridad y privacidad:

| Proveedor | Qué procesan | Propósito |
|---|---|---|
| **Supabase** | Datos de cuenta, mascotas, alertas, avistamientos, fotos, autenticación | Base de datos, autenticación, almacenamiento de archivos, actualizaciones en tiempo real ({{ site.server_region }}) |
| **Google (Firebase Cloud Messaging)** | Token de notificación, contenido de notificaciones | Entrega de notificaciones push |
| **Google (Maps SDK / Play Services location)** | Ubicación del dispositivo, interacciones con el mapa | Mostrar mapas y convertir coordenadas en direcciones |

El uso de información por parte de Google se rige por la [Política de Privacidad de Google](https://policies.google.com/privacy?hl=es). El de Supabase se rige por la [Política de Privacidad de Supabase](https://supabase.com/privacy).

También podemos divulgar información si lo exige la ley, para proteger los derechos y la seguridad de Epona o de sus usuarios, o en relación con una fusión, adquisición o venta de activos (en cuyo caso te avisaremos antes de que tu información quede sujeta a una política de privacidad distinta).

## 5. Transferencias internacionales de datos

Nuestros proveedores de servicio pueden procesar datos fuera del país donde vives, incluyendo {{ site.server_region }} y Estados Unidos. Al usar la App, entiendes que tu información puede transferirse, almacenarse y procesarse en un país con leyes de protección de datos distintas a las tuyas. Cuando corresponda (por ejemplo, para usuarios del Espacio Económico Europeo), estas transferencias se apoyan en las garantías que publican nuestros proveedores (como las Cláusulas Contractuales Tipo).

## 6. Retención de datos

Conservamos tu cuenta y contenido mientras tu cuenta esté activa. Si eliminas tu cuenta (ver [Tus derechos](#8-tus-derechos) abajo), eliminamos tus datos personales, mascotas, alertas y avistamientos — junto con sus fotos — dentro de **30 días**, salvo:
- copias que puedan persistir brevemente en respaldos cifrados hasta que caduquen (típicamente dentro de 30 días), y
- registros anonimizados de reportes de contenido, que podemos conservar hasta **12 meses** por motivos de seguridad y moderación.

## 7. Seguridad

Usamos medidas estándar de la industria para proteger tu información, incluyendo cifrado en tránsito (HTTPS/TLS) y controles de acceso que limitan quién puede leer tus datos en nuestra infraestructura. Ningún método de transmisión o almacenamiento es 100% seguro, y no podemos garantizar seguridad absoluta.

## 8. Tus derechos

Según dónde vivas, puedes tener derechos de acceso, corrección, eliminación o portabilidad de tu información personal, u oposición o restricción de su uso. Sin importar tu ubicación, ofrecemos a todos los usuarios lo siguiente:

- **Acceso y portabilidad:** contáctanos para recibir una copia de tus datos personales.
- **Corrección:** actualiza tu nombre y foto directamente en la App (Perfil). Otros datos pueden corregirse contactándonos.
- **Eliminación:** elimina tu cuenta y datos asociados en cualquier momento desde **Perfil → Eliminar cuenta** dentro de la App, o visitando nuestra [página de eliminación de cuenta](delete-account.html) si ya no tienes la App instalada.
- **Retirar el consentimiento:** cuando dependemos de tu consentimiento (por ejemplo, ubicación), puedes retirarlo desde la configuración de permisos de tu dispositivo, aunque partes de la App podrían dejar de funcionar.

**Venezuela.** Como residente de Venezuela, tienes derechos de hábeas data conforme a los artículos 28 y 60 de la Constitución de la República Bolivariana de Venezuela, para conocer, acceder y solicitar la corrección o eliminación de tu información personal en nuestro poder.

**Espacio Económico Europeo / Reino Unido (GDPR).** Si te encuentras en el EEE o el Reino Unido, tienes los derechos descritos arriba conforme al Reglamento General de Protección de Datos, incluyendo el derecho a presentar una queja ante tu autoridad local de protección de datos. Nuestras bases legales de tratamiento son: ejecución de un contrato (operar la App), consentimiento (por ejemplo, ubicación, notificaciones) e interés legítimo (seguridad, prevención de abuso).

**California.** Los residentes de California tienen derechos conforme a la CCPA/CPRA para conocer, eliminar y corregir información personal, y a no ser discriminados por ejercer estos derechos. No vendemos ni "compartimos" información personal según lo definen esas leyes.

Para ejercer cualquiera de estos derechos, contáctanos en {{ site.contact_email }}.

## 9. Privacidad de menores

Epona está dirigida a usuarios **mayores de 18 años** (ver nuestros [Términos de Servicio](terms.html)). No recopilamos a sabiendas información de menores de 18 años. Si crees que un menor nos proporcionó información, contáctanos en {{ site.contact_email }} y la eliminaremos.

## 10. Cambios a esta política

Podemos actualizar esta política ocasionalmente. Si hacemos cambios materiales, actualizaremos la fecha de "Última actualización" arriba y, cuando corresponda, te notificaremos dentro de la App. Seguir usando la App después de un cambio significa que aceptas la política actualizada.

## 11. Contacto

¿Preguntas sobre esta política o tus datos? Contacta a {{ site.legal_name }} en {{ site.contact_email }}.
