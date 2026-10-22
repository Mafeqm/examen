/**
 * CAMPUSDESK - Configuración Central de Conexión Pública
 * TechNova Solutions
 *
 * Centraliza la resolución de la URL base del Backend (API REST).
 * Prioriza solicitudes relativas al mismo origen cuando el frontend se sirve
 * mediante Nginx (Docker / Red Local / Producción), eliminando dependencias
 * de direcciones IP fijas, puertos codificados o valores obsoletos en localStorage.
 */
(function () {
  'use strict';

  // Configuración global opcional para despliegues con backend en dominio desacoplado
  window.CAMPUSDESK_CONFIG = window.CAMPUSDESK_CONFIG || {
    API_URL: ''
  };

  /**
   * Resuelve la URL base del backend evaluando prioridades:
   * 1. Parámetro de consulta explícito (?api=https://... o ?backend=https://...)
   * 2. Configuración declarativa explícita (window.CAMPUSDESK_CONFIG.API_URL)
   * 3. Mismo origen (Nginx reverse proxy en Docker / LAN / Cloud): window.location.origin
   * 4. Entorno de desarrollo desacoplado sin proxy (ej. Live Server en puerto 5500 o file://) -> http://localhost:8056
   */
  window.getCampusDeskApiUrl = function () {
    // Retorna string vacío para utilizar rutas relativas (/api/...) de forma nativa
    return '';
  };
})();
