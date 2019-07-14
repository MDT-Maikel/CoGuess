/*
 * Copyright © 2019 Maikel de Vries
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.mdtmaikel.coguess;


public class Constants {

    // Word set difficulties.
    public static final int WORDSET_DIFFICULTY_NONE = 0;
    public static final int WORDSET_DIFFICULTY_BASIC = 1;
    public static final int WORDSET_DIFFICULTY_INTERMEDIATE = 2;
    public static final int WORDSET_DIFFICULTY_ADVANCED = 4;
    public static final int WORDSET_DIFFICULTY_ALL = ~0;

    // Word set categories.
    public static final int WORDSET_CATEGORY_NONE = 0;
    public static final int WORDSET_CATEGORY_NATURE = 1;
    public static final int WORDSET_CATEGORY_OBJECTS = 2;
    public static final int WORDSET_CATEGORY_STRUCTURES = 4;
    public static final int WORDSET_CATEGORY_VEHICLES = 8;
    public static final int WORDSET_CATEGORY_CUSTOM = 16;
    public static final int WORDSET_CATEGORY_ALL = ~0;

}