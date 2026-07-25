import { precacheAndRoute } from 'workbox-precaching';

// Workbox precaching entry point
precacheAndRoute(self.__WB_MANIFEST || []);

const CACHE_NAME = 'foodbridge-static-v1';
const API_CACHE_NAME = 'foodbridge-api-v1';

// Caching strategies
self.addEventListener('fetch', (event) => {
  const { request } = event;
  const url = new URL(request.url);

  // Skip non-GET requests
  if (request.method !== 'GET') return;

  // 1. API Calls: NetworkFirst strategy
  if (url.pathname.startsWith('/api/')) {
    event.respondWith(
      fetch(request)
        .then((response) => {
          // Clone response and cache it
          const responseClone = response.clone();
          caches.open(API_CACHE_NAME).then((cache) => {
            cache.put(request, responseClone);
          });
          return response;
        })
        .catch(() => {
          // On network error, try loading from cache
          return caches.match(request);
        })
    );
    return;
  }

  // 2. Static Assets (JS, CSS, Images, Fonts): CacheFirst strategy
  const isStaticAsset = 
    url.pathname.match(/\.(js|css|png|jpg|jpeg|svg|woff2|woff|ttf|ico)$/) ||
    url.origin === self.location.origin;

  if (isStaticAsset) {
    event.respondWith(
      caches.match(request).then((cachedResponse) => {
        if (cachedResponse) {
          // Return cached version but refresh in background (stale-while-revalidate style)
          fetch(request).then((networkResponse) => {
            if (networkResponse.status === 200) {
              caches.open(CACHE_NAME).then((cache) => {
                cache.put(request, networkResponse);
              });
            }
          }).catch(() => { /* ignore */ });
          
          return cachedResponse;
        }

        // Otherwise fetch and cache
        return fetch(request).then((networkResponse) => {
          if (networkResponse.status === 200) {
            const responseClone = networkResponse.clone();
            caches.open(CACHE_NAME).then((cache) => {
              cache.put(request, responseClone);
            });
          }
          return networkResponse;
        });
      })
    );
  }
});

// Push notification listener
self.addEventListener('push', (event) => {
  let data = {};
  if (event.data) {
    try {
      data = event.data.json();
    } catch (e) {
      data = { body: event.data.text() };
    }
  }

  const title = data.title || "New food nearby!";
  const options = {
    body: data.body || "A new food donation is available.",
    icon: '/icons/icon-192.png',
    badge: '/icons/icon-192.png',
    data: {
      url: data.url || '/ngo/dashboard'
    },
    actions: [
      { action: 'claim', title: 'Claim now' },
      { action: 'close', title: 'Dismiss' }
    ],
    vibrate: [100, 50, 100],
    requireInteraction: true
  };

  event.waitUntil(
    self.registration.showNotification(title, options)
  );
});

// Notification click listener
self.addEventListener('notificationclick', (event) => {
  event.notification.close();

  if (event.action === 'close') {
    return;
  }

  const urlToOpen = event.notification.data?.url || '/ngo/dashboard';

  event.waitUntil(
    clients.matchAll({ type: 'window', includeUncontrolled: true }).then((windowClients) => {
      // Focus if tab already open
      for (let i = 0; i < windowClients.length; i++) {
        const client = windowClients[i];
        if (client.url.includes(urlToOpen) && 'focus' in client) {
          return client.focus();
        }
      }
      // Otherwise open new tab
      if (clients.openWindow) {
        return clients.openWindow(urlToOpen);
      }
    })
  );
});
