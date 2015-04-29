package com.nxt.nxtvault.util;

public abstract class NxtException extends Exception {

    /**
   * 
   */
  private static final long serialVersionUID = 1L;

    protected NxtException() {
        super();
    }

    protected NxtException(String message) {
        super(message);
    }

    protected NxtException(String message, Throwable cause) {
        super(message, cause);
    }

    protected NxtException(Throwable cause) {
        super(cause);
    }

    public static abstract class ValidationException extends NxtException {

        /**
       * 
       */
      private static final long serialVersionUID = 1L;

        private ValidationException(String message) {
            super(message);
        }

        private ValidationException(String message, Throwable cause) {
            super(message, cause);
        }

    }

    public static class NotCurrentlyValidException extends ValidationException {

        /**
       * 
       */
      private static final long serialVersionUID = 1L;

        public NotCurrentlyValidException(String message) {
            super(message);
        }

        public NotCurrentlyValidException(String message, Throwable cause) {
            super(message, cause);
        }

    }

    public static final class NotYetEnabledException extends NotCurrentlyValidException {

        /**
       * 
       */
      private static final long serialVersionUID = 1L;

        public NotYetEnabledException(String message) {
            super(message);
        }

        public NotYetEnabledException(String message, Throwable throwable) {
            super(message, throwable);
        }

    }

    public static final class NotValidException extends ValidationException {

        /**
       * 
       */
      private static final long serialVersionUID = 1L;

        public NotValidException(String message) {
            super(message);
        }

        public NotValidException(String message, Throwable cause) {
            super(message, cause);
        }

    }
    
    public static final class NxtApiException extends NxtException {

      /**
     * 
     */
    private static final long serialVersionUID = 1L;

      public NxtApiException(String message) {
          super(message);
      }

      public NxtApiException(String message, Throwable cause) {
          super(message, cause);
      }

  }

}
