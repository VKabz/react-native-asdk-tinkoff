/* eslint-disable react-native/no-inline-styles */
import * as React from 'react';
import { Text, View, TouchableHighlight } from 'react-native';
import SDK from 'react-native-asdk-tinkoff';

export default () => {
  const sdk = new SDK({
    TerminalKey: '1600075810756DEMO',
    Password: '8rue0p6uc8al76i4',
    PublicKey:
      'MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAv5yse9ka3ZQE0feuGtemYv3IqOlLck8zHUM7lTr0za6lXTszRSXfUO7jMb+L5C7e2QNFs+7sIX2OQJ6a+HG8kr+jwJ4tS3cVsWtd9NXpsU40PE4MeNr5RqiNXjcDxA+L4OsEm/BlyFOEOh2epGyYUd5/iO3OiQFRNicomT2saQYAeqIwuELPs1XpLk9HLx5qPbm8fRrQhjeUD5TLO8b+4yCnObe8vy/BMUwBfq+ieWADIjwWCMp2KTpMGLz48qnaD9kdrYJ0iyHqzb2mkDhdIzkim24A3lWoYitJCBrrB2xM05sm9+OdCI1f7nPNJbl5URHobSwR94IRGT7CJcUjvwIDAQAB',
  });

  return (
    <View style={{ flex: 1, alignItems: 'center', justifyContent: 'center' }}>
      <TouchableHighlight
        onPress={async () => {
          //
          const e = await sdk.Pay({
            Amount: 1000,
            OrderId: 13,
            CustomerKey: 'TEST3',
          });
          console.warn(e);
        }}
      >
        <View style={{ width: '100%', padding: 16 }}>
          <Text>Pay()</Text>
        </View>
      </TouchableHighlight>
    </View>
  );
};
