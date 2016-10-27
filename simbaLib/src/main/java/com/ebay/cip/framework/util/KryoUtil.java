package com.ebay.cip.framework.util;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by jagmehta on 6/16/2015.
 * This class is specifically dealing with improving performance of Kryo and it is highly encouraged that we use this class for Kryo operations.
 */
public class KryoUtil {

    /** Setup ThreadLocal of Kryo instances
     * Below code sets up kryo object per thread. Since Kryo itself is not thread safe, this is necessary to do
     * to optimize performance.
     *
     * It also registers its custom code for java collections asList & subList classes so its available out of tbe box.
     */
    private static ThreadLocal<Kryo> kryos = new ThreadLocal<Kryo>() {
        protected Kryo initialValue() {
            Kryo kryo = new Kryo();

            /* configure kryo instance, customize settings */

            Serializer<List> customListSerializer = new Serializer<List>() {
                @Override
                public void write(Kryo kryo, Output output, List object) {
                    kryo.writeObject(output, object.toArray(new Object[object.size()]));
                }
                @Override
                public List read(Kryo kryo, Input input, Class<List> type) {
                    return Arrays.asList(kryo.readObject(input, Object[].class));
                }
            };

            //Register custom code for asList()
            kryo.register(Arrays.asList().getClass(), customListSerializer);
            String[] strArray = new String[] {"1","2","3"};
            kryo.register(Arrays.asList(strArray).subList(0, 1).getClass(), customListSerializer);

            /** Create a dummy ArrayList to register **/
            ArrayList<String> str = new ArrayList<>();
            str.add("1");
            str.add("2");

            //Register custom code for subList
            kryo.register(str.subList(0, 1).getClass(), customListSerializer);

/*
            //Another way of doing it. But it does not serve all objects return by asList
            kryo.register(Arrays.asList().getClass(), new CollectionSerializer() {
                protected Collection create(Kryo kryo, Input input, Class<Collection> type) {
                    return new ArrayList();
                }
            });

*/


            /************  Serializer for Charsets *******************/

            Serializer charsetCustomListSerializer = new Serializer() {
                @Override
                public void write(Kryo kryo, Output output, Object object) {
                    Charset charset = (Charset)object;
                    kryo.writeObject(output, charset.name());
                }

                @Override
                public Charset read(Kryo kryo, Input input, Class type) {
                    return Charset.forName(kryo.readObject(input, String.class));
                }

            };
            // Register those Charset classes which are not public.
            registerCharsets(kryo,"UTF-8",charsetCustomListSerializer);
            registerCharsets(kryo,"UTF-16",charsetCustomListSerializer);
            registerCharsets(kryo,"UTF-16BE",charsetCustomListSerializer);
            registerCharsets(kryo,"UTF-16LE",charsetCustomListSerializer);
            registerCharsets(kryo,"ISO_8859_1",charsetCustomListSerializer);
            //registerCharsets(kryo,"US_ASCII",charsetCustomListSerializer);

            /*********************************************************/



            return kryo;
        }

        /**
         * Safe method to register CharSets. It ignores any exception and moves on.
         * @param kryo
         * @param name
         * @param s
         * @return
         */
        private boolean registerCharsets(Kryo kryo, String name, Serializer s){
            try {
                kryo.register(Charset.forName(name).getClass(), s);
                return true;
            }catch (Throwable e) {
                System.err.println("[WARN] Unable to register charset "+name+" with Kryo. Exception is "+ e.getMessage());
                e.printStackTrace();
                return false;
            }
        }

    };

    /**
     * Serialize given object as bytes using Kryo.
     *
     * @param o
     * @return byte[] serialized byte[]
     */
    public static byte[] serialize(Object o) {

        Kryo kryo = kryos.get();
        ByteArrayOutputStream bst = new ByteArrayOutputStream();
        Output out = new Output(bst);
        kryo.writeObject(out, o);
        out.close();
        return bst.toByteArray();
//        return "dummy string".getBytes();

    }

    /**
     * Deserialize kryo serialized byte[] into given class.
     *
     * @param bytes
     * @param claz
     * @param <T>
     * @return
     */
    public static <T> T deserialize(byte[] bytes, Class<T> claz) {
        Kryo kryo = kryos.get();
        Input input = new Input(bytes);
        T obj = kryo.readObjectOrNull(input, claz);
        return obj;
    }




































//
//    /**
//     * Testing it in paraller scenarios
//     *
//     * @param argv
//     * @throws InterruptedException
//     */
//    public static void main(String... argv) throws InterruptedException, IOException {
//
//        class SomeClass {
//            public int id = 0;
//            public String name = "some";
//            public JobTypeEnum myType = JobTypeEnum.DUMMY_JOB;
//
//            public HashMap<Integer, String> map = new HashMap<Integer, String>();
//            public HashMap<String, Map<Integer, String>> mapmap = new HashMap<String, Map<Integer, String>>();
//
//
//            public SomeClass() {
//            }
//
//            public SomeClass(int id, String name) {
//                this.id = id;
//                this.name = name;
//                map.put(1, "1");
//                map.put(2, "2");
//                mapmap.put("someKey", map);
//            }
//        }
//
//        class Serializer extends Thread {
//            public SomeClass c;
//            public byte[] bytes;
//
//            @Override
//            public void run() {
//                System.out.println("Thread id->" + Thread.currentThread().getName());
//                bytes = KryoUtil.serialize(c);
//            }
//        }
//
//        class Deserializer extends Thread {
//            public SomeClass c;
//            public byte[] bytes;
//
//            @Override
//            public void run() {
//                System.out.println("Thread id->" + Thread.currentThread().getName());
//                c = KryoUtil.deserialize(bytes, SomeClass.class);
//            }
//        }
//
//        SomeClass s1 = new SomeClass(1, "jagrut");
//        SomeClass s2 = new SomeClass();
//        s2.id = 100;
//
//        byte[] bytes = serialize(s1);
//        SomeClass s = deserialize(bytes, SomeClass.class);
//        System.out.println(s.id + " " + s.name + " " + s.myType.getId() + " " + s.myType.getName());
//        System.out.println(s.myType == JobTypeEnum.DUMMY_JOB);
//
//        bytes = serialize(s2);
//        s = deserialize(bytes, SomeClass.class);
//        System.out.println(s.id + " " + s.name + " " + s.myType.getId() + " " + s.myType.getName());
//
//        System.out.println("-----Using thread in paraller ----");
//
//
//        ExecutorService executorService = Executors.newFixedThreadPool(50);
//
//
//        int size = 1;
//        SomeClass[] sc = new SomeClass[size];
//        Serializer[] se = new Serializer[size];
//        Deserializer[] de = new Deserializer[size];
//        for (int i = 0; i < size; i++) {
//            sc[i] = new SomeClass();
//            sc[i].id = i;
//            se[i] = new Serializer();
//            se[i].c = sc[i];
//            executorService.submit(se[i]);
//        }
//        //executorService.
//
//        Thread.sleep(size * 100);
//
//        System.out.println("\n-----Deserilizing ----\n");
//
//        for (int i = 0; i < size; i++) {
//            de[i] = new Deserializer();
//            de[i].bytes = se[i].bytes;
//            executorService.submit(de[i]);
//        }
//
//        Thread.sleep(size * 100);
//        for (int i = 0; i < size; i++) {
//            System.out.println(de[i].c.id);
//        }
//
//        executorService.shutdown();
//
//
///*
//        System.out.println("\n---- Json test ----\n");
//        ObjectMapper mapper = new ObjectMapper();
//        String json = mapper.writeValueAsString(JobTypeEnum.DUMMY_JOB);
//        System.out.println(json);
//        JobTypeEnum je = mapper.readValue(json,JobTypeEnum.class);
//        System.out.println(je == JobTypeEnum.DUMMY_JOB);
//*/
//
//
//    }

}

