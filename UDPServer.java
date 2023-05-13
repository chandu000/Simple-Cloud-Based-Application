package server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

public class UDPServer {
    public static void main(String[] args) {
        DatagramSocket ds = null;
        InetAddress ia = null;
        try {
            ds = new DatagramSocket(1001);
            ia = InetAddress.getLocalHost();
        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        while(true) {
            byte[] filename = new byte[1000];
            byte[] b = new byte[10000000];

            DatagramPacket name = new DatagramPacket(filename,filename.length);
            try {
                ds.receive(name);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            String rfname = new String(name.getData()).trim();
            System.out.println("received name:"+rfname);
            String rootname = rfname.split("\\.")[0].split("(?<=\\D)(?=\\d)")[0]+"."+rfname.split("\\.")[1].trim();
            DatagramPacket p = new DatagramPacket(b,b.length);
            try {
                ds.receive(p);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            String fname = new String(name.getData()).trim();
            fname = "/Users/chandupavanbudda/Desktop/file  sync/src/server folder/"+fname;

            String filedata = new String(p.getData()).trim();
            String code = bytesToHex(digest(filedata.getBytes(),"SHA-256"));
            System.out.println("receive:"+rfname+","+code);
            PrintWriter pw = null;
            try {
                pw = new PrintWriter(fname);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            pw.println(filedata);
            pw.close();


            File tempfile = new File("/Users/chandupavanbudda/Desktop/file  sync/src/server folder/.metafile.json");
            String tempdata = "";
            try {
                tempdata = FileUtils.readFileToString(tempfile, "UTF-8");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            HashMap<String,LinkedHashMap<String,String>> hmap = new HashMap<>();
            JSONArray jsonarray = new JSONArray();
            if(tempdata.length() > 0) {
                JSONArray jsonArray = null;
                try {
                    jsonArray = (JSONArray) new JSONParser().parse(tempdata);
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                for(int i=0;i<jsonArray.size();i++) {
                    JSONObject json = (JSONObject) jsonArray.get(i);
                    HashSet<String> keys = new HashSet<>(json.keySet());
                    for(String s : keys) {
                        JSONObject cjson = (JSONObject) json.get(s);
                        try {
                            LinkedHashMap<String,String> t = (LinkedHashMap<String, String>) new ObjectMapper().readValue(cjson.toString(), Map.class);
                            hmap.put(s,t);
                        } catch (JsonMappingException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (JsonProcessingException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                boolean fileExist = false;
                for(Map.Entry<String, LinkedHashMap<String,String>> m : hmap.entrySet()) {
                    if(m.getKey().equals(rootname)) {
                        fileExist = true;
                        LinkedHashMap<String,String> cm = m.getValue();
                        boolean cfileExist = false;
                        for(Map.Entry<String, String> childm : cm.entrySet()) {
                            if(childm.getKey().equals(rfname)) {
                                cm.put(childm.getKey(), code);
                                cfileExist = true;
                                break;
                            }
                        }
                        if(!cfileExist) {
                            cm.put(rfname, code);
                        }
                    }
                }
                if(!fileExist) {
                    LinkedHashMap<String,String> t = new LinkedHashMap<>();
                    t.put(rfname, code);
                    hmap.put(rootname,t);
                }
                jsonarray.add(hmap);
            }else {
                LinkedHashMap<String, String> temp = new LinkedHashMap<>();
                temp.put(rfname,code);
                hmap.put(rootname, temp);
                jsonarray.add(hmap);
            }

            System.out.println(jsonarray);
            String path = "/Users/chandupavanbudda/Desktop/file  sync/src/server folder/.metafile.json";

            try (PrintWriter out = new PrintWriter(new FileWriter(path))) {
                out.write(jsonarray.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


    public static byte[] digest(byte[] input, String algorithm) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        }
        byte[] result = md.digest(input);
        return result;
    }

    public static String bytesToHex(byte[] bytes) {
        return new BigInteger(1, bytes).toString(16);
    }



}
