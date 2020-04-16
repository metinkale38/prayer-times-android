/*
 * Copyright (c) 2013-2019 Metin Kale
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

package com.metinkale.prayer.times.times.sources;

import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.DataEmitter;
import com.koushikdutta.async.DataSink;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.TransformFuture;
import com.koushikdutta.async.parser.AsyncParser;
import com.koushikdutta.async.parser.StringParser;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.future.ResponseFuture;
import com.metinkale.prayer.App;

import org.joda.time.LocalDate;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * might be used at some time, currently not in use
 */
public class DiyanetSOAPClient {
    private static final String URL = "http://namazvakti.diyanet.gov.tr/wsNamazVakti.svc";
    private static final String USER = "namazuser";
    private static final String PASSWORD = "NamVak!14";
    private static final String AUTH_TAGS =
            "<ns1:username>" + USER + "</ns1:username><ns1:password>" + PASSWORD + "</ns1:password>";

    static class Times {
        LocalDate date;
        String fajr;
        String sun;
        String dhuhr;
        String asr;
        String maghrib;
        String ishaa;
    }

    public static ResponseFuture<List<Times>> getTimes(int cityId) {


        return Ion.with(App.get()).load(URL).userAgent(App.getUserAgent())
                .setHeader("Content-Type", "text/xml; charset=utf-8").setHeader("SOAPAction", "http://tempuri.org/IwsNamazVakti/AylikNamazVakti")
                .setStringBody("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                        "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"http://tempuri.org/\">" +
                        "<SOAP-ENV:Body>" +
                        "<ns1:AylikNamazVakti>" +
                        "<ns1:IlceID>" + cityId + "</ns1:IlceID>" +
                        AUTH_TAGS +
                        "</ns1:AylikNamazVakti>" +
                        "</SOAP-ENV:Body>" +
                        "</SOAP-ENV:Envelope>").as(new AsyncParser<List<Times>>() {
                    @Override
                    public Future<List<Times>> parse(DataEmitter emitter) {
                        return new StringParser
                                ().parse(emitter).then(new TransformFuture<List<Times>, String>() {
                            @Override
                            protected void transform(String result) {
                                List<Times> allTimes = new ArrayList<>();
                                result = result.substring(result.indexOf("<a:NamazVakti>") + 14);
                                result = result.substring(0, result.indexOf("</AylikNamazVaktiResult>"));
                                String[] days = result.split("</a:NamazVakti><a:NamazVakti>");
                                for (String day : days) {
                                    String[] parts = day.split("><a:");

                                    String[] times = new String[6];
                                    String date = null;
                                    Times t = new Times();
                                    for (String part : parts) {
                                        if (!part.contains(">"))
                                            continue;
                                        String name = part.substring(0, part.indexOf('>'));
                                        if (name.contains(":"))
                                            name = name.substring(name.indexOf(':') + 1);
                                        String content = part.substring(part.indexOf('>') + 1);
                                        content = content.substring(0, content.indexOf('<'));

                                        switch (name) {
                                            case "Imsak":
                                                t.fajr = content;
                                                break;
                                            case "Gunes":
                                                t.sun = content;
                                                break;
                                            case "Ogle":
                                                t.dhuhr = content;
                                                break;
                                            case "Ikindi":
                                                t.asr = content;
                                                break;
                                            case "Aksam":
                                                t.maghrib = content;
                                                break;
                                            case "Yatsi":
                                                t.ishaa = content;
                                                break;
                                            case "MiladiTarihKisa":
                                                date = content;
                                                break;
                                        }
                                    }
                                    String[] d = date.split("\\.");
                                    t.date = new LocalDate(Integer.parseInt(d[2]), Integer.parseInt(d[1]), Integer.parseInt(d[0]));
                                    allTimes.add(t);
                                }
                                setComplete(allTimes);
                            }
                        });
                    }

                    @Override
                    public void write(DataSink sink, List<Times> value, CompletedCallback completed) {

                    }

                    @Override
                    public Type getType() {
                        return new TypeToken<List<Times>>() {
                        }.getType();
                    }
                });
    }


    static class Scope {
        String nameTr;
        String nameEn;
        int id;
    }

    public static ResponseFuture<List<Scope>> getCountries() {
        return getScope(ScopeType.Country, 0);
    }

    public static ResponseFuture<List<Scope>> getStates(int countryId) {
        return getScope(ScopeType.State, countryId);
    }

    public static ResponseFuture<List<Scope>> getCities(int stateId) {
        return getScope(ScopeType.City, stateId);
    }

    private enum ScopeType {Country, State, City}

    private static ResponseFuture<List<Scope>> getScope(ScopeType type, int parent) {
        final String plural;
        final String singular;
        final String identifierTag;
        switch (type) {

            case Country:
                plural = "Ulkeler";
                singular = "Ulke";
                identifierTag = "";
                break;
            case State:
                plural = "Sehirler";
                singular = "Sehir";
                identifierTag = "<ns1:UlkeID>" + parent + "</ns1:UlkeID>";
                break;
            case City:
                plural = "Ilceler";
                singular = "Ilce";
                identifierTag = "<ns1:EyaletSehirID>" + parent + "</ns1:EyaletSehirID>";
                break;
            default:
                plural = null;
                singular = null;
                identifierTag = null;
        }


        return Ion.with(App.get()).load(URL).userAgent(App.getUserAgent())
                .setHeader("Content-Type", "text/xml; charset=utf-8").setHeader("SOAPAction", "http://tempuri.org/IwsNamazVakti/AylikNamazVakti")
                .setStringBody("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                        "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"http://tempuri.org/\">" +
                        "<SOAP-ENV:Body>" +
                        "<ns1:" + plural + ">" +
                        identifierTag +
                        AUTH_TAGS +
                        "</ns1:" + plural + ">" +
                        "</SOAP-ENV:Body>" +
                        "</SOAP-ENV:Envelope>").as(new AsyncParser<List<Scope>>() {
                    @Override
                    public Future<List<Scope>> parse(DataEmitter emitter) {
                        return new StringParser
                                ().parse(emitter).then(new TransformFuture<List<Scope>, String>() {
                            @Override
                            protected void transform(String result) {
                                List<Scope> allScopes = new ArrayList<>();
                                String[] scopes = result.split("<a:" + singular + ">");
                                for (String scope : scopes) {
                                    Scope c = new Scope();
                                    c.nameTr = scope.substring(scope.indexOf("<a:" + singular + "Adi>") + ("<a:" + singular + "Adi>").length());
                                    c.nameEn = scope.substring(scope.indexOf("<a:" + singular + "AdiEn>") + ("<a:" + singular + "AdiEn>").length());
                                    String id = scope.substring(scope.indexOf("<a:" + singular + "ID>") + ("<a:" + singular + "ID>").length());

                                    c.nameTr = c.nameTr.substring(0, c.nameTr.indexOf("<"));
                                    c.nameEn = c.nameEn.substring(0, c.nameEn.indexOf("<"));
                                    c.id = Integer.parseInt(id.substring(0, id.indexOf("<")));
                                    allScopes.add(c);
                                }

                                setComplete(allScopes);
                            }
                        });
                    }

                    @Override
                    public void write(DataSink sink, List<Scope> value, CompletedCallback completed) {

                    }

                    @Override
                    public Type getType() {
                        return new TypeToken<List<Scope>>() {
                        }.getType();
                    }
                });
    }


}
