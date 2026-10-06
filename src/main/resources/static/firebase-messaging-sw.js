// Service worker nhan thong bao day (Firebase Cloud Messaging) khi tab chat dang an hoac da dong.
// Phai nam o goc web (/firebase-messaging-sw.js). Cau hinh Firebase duoc truyen qua query string luc dang ky
// (xem chat-test.html), vi Firebase yeu cau khoi tao ngay khi service worker chay.
importScripts('https://www.gstatic.com/firebasejs/12.19.0/firebase-app-compat.js');
importScripts('https://www.gstatic.com/firebasejs/12.19.0/firebase-messaging-compat.js');

const params = new URL(self.location).searchParams;
firebase.initializeApp({
  apiKey: params.get('apiKey'),
  authDomain: params.get('authDomain'),
  projectId: params.get('projectId'),
  storageBucket: params.get('storageBucket'),
  messagingSenderId: params.get('messagingSenderId'),
  appId: params.get('appId')
});

// Server gui dang data-only: tu hien thong bao o day.
// Chi chay khi khong co tab nao cua trang dang hien; tab dang mo thi trang tu hien thong bao trong trang.
firebase.messaging().onBackgroundMessage(payload => {
  const d = payload.data || {};
  if (d.kind !== 'chat.message') return;
  return self.registration.showNotification(d.title || 'Tin nhắn mới', {
    body: d.body || '',
    tag: d.conversationId,   // cung hoi thoai thi thay thong bao cu, khong chat dong
    renotify: true,
    data: d
  });
});

self.addEventListener('notificationclick', event => {
  event.notification.close();
  const d = event.notification.data || {};
  const url = `/chat-test.html?me=${encodeURIComponent(d.recipientId || '')}`
    + `&c=${encodeURIComponent(d.conversationId || '')}&with=${encodeURIComponent(d.senderId || '')}`;
  event.waitUntil((async () => {
    const tabs = await clients.matchAll({ type: 'window', includeUncontrolled: true });
    const tab = tabs.find(t => new URL(t.url).pathname === '/chat-test.html');
    if (tab) {
      await tab.focus();
      tab.postMessage({ type: 'chat.open', ...d });
    } else {
      await clients.openWindow(url);
    }
  })());
});
