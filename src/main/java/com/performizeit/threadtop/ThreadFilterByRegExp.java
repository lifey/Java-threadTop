/*
 *
 * Copyright 2011 Performize-IT LTD.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.performizeit.threadtop;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


class ThreadFilterByRegExp implements ThreadFilter {

    Pattern pattern;

    public ThreadFilterByRegExp(String regexp) {
        pattern = Pattern.compile(regexp);
    }

    public boolean matchFilter(MyThreadInfo ti) {
        Matcher matcher = pattern.matcher(ti.getName());
        return matcher.matches();


    }
}
