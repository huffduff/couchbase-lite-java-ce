package com.couchbase.lite.utils;

//
// Copyright (c) 2019 Couchbase, Inc All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
public interface Fn {
    interface Function<T, R> { R apply(T x); }
    interface Predicate<T> { boolean test(T x); }
    interface Provider<T> { T get(); }
    interface Consumer<T> { void accept(T x); }
}
