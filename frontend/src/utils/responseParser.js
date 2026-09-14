/**
 * Utility to unpack data arrays from backend API responses across all Spring Boot / Axios shapes:
 * - AxiosResponse -> ApiResponse -> PageResponse (res.data.data.content)
 * - AxiosResponse -> ApiResponse -> List (res.data.data)
 * - AxiosResponse -> PageResponse (res.data.content)
 * - Axios Interceptor -> ApiResponse -> PageResponse (res.data.content)
 * - Axios Interceptor -> ApiResponse -> List (res.data)
 * - Axios Interceptor -> PageResponse (res.content)
 * - Direct Array (res)
 * 
 * Returns Array if found, or null if no valid list structure exists.
 */
export function extractList(res) {
  if (!res) return null;
  if (Array.isArray(res)) return res;
  if (Array.isArray(res.data?.data?.content)) return res.data.data.content;
  if (Array.isArray(res.data?.content)) return res.data.content;
  if (Array.isArray(res.data?.data)) return res.data.data;
  if (Array.isArray(res.data)) return res.data;
  if (Array.isArray(res.content)) return res.content;
  return null;
}
