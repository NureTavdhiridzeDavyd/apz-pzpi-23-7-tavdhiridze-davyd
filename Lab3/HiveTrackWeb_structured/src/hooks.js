import { useCallback, useEffect, useRef, useState } from 'react';

export function useApiData(api, path, onError) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [loaded, setLoaded] = useState(false);
  const onErrorRef = useRef(onError);

  useEffect(() => {
    onErrorRef.current = onError;
  }, [onError]);

  const load = useCallback(async () => {
    if (!path) {
      return;
    }

    setLoading(true);
    setError('');

    try {
      const result = await api.get(path);
      setData(result);
      setLoaded(true);
    } catch (err) {
      const message = err?.message || 'Помилка завантаження даних';
      setError(message);
      setLoaded(true);
      onErrorRef.current?.(message);
    } finally {
      setLoading(false);
    }
  }, [api, path]);

  useEffect(() => {
    let active = true;

    async function loadInitial() {
      if (!path) {
        return;
      }

      setLoading(true);
      setError('');

      try {
        const result = await api.get(path);
        if (!active) return;
        setData(result);
        setLoaded(true);
      } catch (err) {
        if (!active) return;
        const message = err?.message || 'Помилка завантаження даних';
        setError(message);
        setLoaded(true);
        onErrorRef.current?.(message);
      } finally {
        if (active) setLoading(false);
      }
    }

    loadInitial();

    return () => {
      active = false;
    };
  }, [api, path]);

  return {
    data,
    loading,
    loaded,
    error,
    reload: load
  };
}
