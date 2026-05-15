import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: 25,
  duration: '30s',
  thresholds: {
    http_req_failed: ['rate<0.05'],
    http_req_duration: ['p(95)<1200']
  }
};

export default function () {
  const baseUrl = 'http://host.docker.internal:8080';

  const health = http.get(`${baseUrl}/api/health`);
  check(health, {
    'health status 200': (res) => res.status === 200
  });

  const instance = http.get(`${baseUrl}/api/instance`);
  check(instance, {
    'instance status 200': (res) => res.status === 200
  });

  const groups = http.get(`${baseUrl}/api/groups`);
  check(groups, {
    'groups status 200': (res) => res.status === 200
  });

  sleep(1);
}
