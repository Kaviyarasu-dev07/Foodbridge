import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import LanguageDetector from 'i18next-browser-languagedetector';

import enTranslation from './locales/en/translation.json';
import taTranslation from './locales/ta/translation.json';

i18n
  // detect user language
  .use(LanguageDetector)
  // pass the i18n instance to react-i18next.
  .use(initReactI18next)
  // init i18next
  .init({
    resources: {
      en: { translation: enTranslation },
      ta: { translation: taTranslation }
    },
    fallbackLng: 'en',
    detection: {
      order: ['localStorage', 'navigator'],
      caches: ['localStorage']
    },
    interpolation: {
      escapeValue: false // not needed for react as it escapes by default
    },
    react: {
      useSuspense: false // Disable Suspense to prevent blank white screens in React 19
    }
  });

export default i18n;
