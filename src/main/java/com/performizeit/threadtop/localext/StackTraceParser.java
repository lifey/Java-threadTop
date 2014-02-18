package com.performizeit.threadtop.localext;

import javax.management.openmbean.CompositeData;

/**
 * Stack Trace parser
 * User: lyanm
 */
public class StackTraceParser {
    /**
     * Parse stack trace elements from thread CompositeData[]
     */
    public static String[] parseStackTrace(CompositeData[] elements) {
        if(elements == null) {
            return new String[] {"N/A"};
        }
        String[] result = new String[elements.length];
        StringBuilder sb = new StringBuilder();

        for (int i=0; i < elements.length; i++)
        {
            CompositeData stackTraceElement = elements[i];

            String className = String.valueOf(stackTraceElement.get("className"));
            String methodName = String.valueOf(stackTraceElement.get("methodName"));
            boolean isNative = (Boolean) stackTraceElement.get("nativeMethod");
            String filename = String.valueOf(stackTraceElement.get("fileName"));
            int lineNumber = (Integer) stackTraceElement.get("lineNumber");

            sb.append(className);
            sb.append(".");
            sb.append(methodName);

            sb.append("(");

            if (isNative)
            {
                sb.append("Native Method");
            }
            else
            {
                sb.append(filename);

                if (lineNumber > 0)
                {
                    sb.append(":").append(lineNumber);
                }
            }
            sb.append(")");
            result[i] = sb.toString();
            sb.setLength(0);
        }
        return result;
    }
}
