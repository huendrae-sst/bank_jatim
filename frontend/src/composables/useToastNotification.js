import { toast } from '../utils/toast.js';

/**
 * Composable for easy toast notifications in Vue setup script.
 * @returns {typeof toast}
 */
export function useToastNotification() {
  return toast;
}

export default useToastNotification;
